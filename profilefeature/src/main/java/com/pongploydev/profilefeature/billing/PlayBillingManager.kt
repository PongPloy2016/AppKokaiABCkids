package com.pongploydev.profilefeature.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * PlayBillingManager handles Google Play Billing Library Subscriptions and In-App Purchases
 * according to official Android developer guidelines:
 * https://developer.android.com/google/play/billing/subscriptions
 */
class PlayBillingManager private constructor(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PlayBillingManager"

        // Standard Product IDs configured in Google Play Console
        const val PRODUCT_SUB_MONTHLY = "sub_monthly"
        const val PRODUCT_SUB_YEARLY = "sub_yearly"
        const val PRODUCT_LIFETIME = "sub_lifetime"

        @Volatile
        private var instance: PlayBillingManager? = null

        fun getInstance(context: Context): PlayBillingManager {
            return instance ?: synchronized(this) {
                instance ?: PlayBillingManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private val dataManager = SupabaseDataManager.getInstance(context)

    // State flows for UI observation
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _activePurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val activePurchases: StateFlow<List<Purchase>> = _activePurchases.asStateFlow()

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        startConnection()
    }

    /**
     * Start connection to Google Play Billing Service
     */
    fun startConnection(onReady: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            _isConnected.value = true
            queryAllProducts()
            queryPurchases()
            onReady?.invoke()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing setup successful.")
                    _isConnected.value = true
                    queryAllProducts()
                    queryPurchases()
                    onReady?.invoke()
                } else {
                    Log.w(TAG, "Google Play Billing setup failed: ${billingResult.debugMessage} (code: ${billingResult.responseCode})")
                    _isConnected.value = false
                    _billingStatusMessage.value = "เชื่อมต่อ Google Play ไม่สำเร็จ (${billingResult.responseCode})"
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Google Play Billing service disconnected. Reconnecting...")
                _isConnected.value = false
            }
        })
    }

    /**
     * Query subscription and in-app product details from Google Play Console
     */
    fun queryAllProducts() {
        if (!billingClient.isReady) {
            Log.w(TAG, "BillingClient is not ready to query products.")
            return
        }

        // 1. Query Subscriptions (Monthly, Yearly)
        val subscriptionProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_SUB_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_SUB_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subscriptionProducts)
            .build()

        billingClient.queryProductDetailsAsync(subParams) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsList = queryProductDetailsResult.productDetailsList
                Log.d(TAG, "Subscriptions fetched successfully: ${productDetailsList.size} products found.")
                val current = _productDetailsMap.value.toMutableMap()
                for (details in productDetailsList) {
                    current[details.productId] = details
                }
                _productDetailsMap.value = current
            } else {
                Log.w(TAG, "Failed to query subscriptions: ${billingResult.debugMessage}")
            }
        }

        // 2. Query In-App One-Time Product (Lifetime)
        val inAppProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProducts)
            .build()

        billingClient.queryProductDetailsAsync(inAppParams) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsList = queryProductDetailsResult.productDetailsList
                Log.d(TAG, "In-app products fetched: ${productDetailsList.size} found.")
                val current = _productDetailsMap.value.toMutableMap()
                for (details in productDetailsList) {
                    current[details.productId] = details
                }
                _productDetailsMap.value = current
            } else {
                Log.w(TAG, "Failed to query in-app products: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Query existing active purchases and subscriptions to restore entitlements
     */
    fun queryPurchases(onComplete: ((Boolean) -> Unit)? = null) {
        if (!billingClient.isReady) {
            startConnection { queryPurchases(onComplete) }
            return
        }

        var subsDone = false
        var inAppDone = false
        var anyActiveFound = false

        // Query active subscriptions
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subsParams) { billingResult, purchasesList ->
            subsDone = true
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        anyActiveFound = true
                        handlePurchase(purchase)
                    }
                }
                _activePurchases.value = purchasesList
            }
            if (subsDone && inAppDone) onComplete?.invoke(anyActiveFound)
        }

        // Query active in-app one-time purchases (lifetime)
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchasesList ->
            inAppDone = true
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        anyActiveFound = true
                        handlePurchase(purchase)
                    }
                }
            }
            if (subsDone && inAppDone) onComplete?.invoke(anyActiveFound)
        }
    }

    /**
     * Launch Google Play Billing Flow for a chosen product
     */
    fun launchBillingFlow(
        activity: Activity,
        productId: String,
        onResult: (BillingResult) -> Unit
    ) {
        if (!billingClient.isReady) {
            Log.e(TAG, "Cannot launch billing flow: BillingClient is not connected.")
            onResult(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                    .setDebugMessage("ระบบ Google Play ยังไม่ได้เชื่อมต่อ กรุณาลองใหม่อีกครั้ง")
                    .build()
            )
            startConnection()
            return
        }

        val productDetails = _productDetailsMap.value[productId]
        if (productDetails == null) {
            Log.w(TAG, "ProductDetails for $productId not found in cache. Attempting fresh query...")
            queryAllProducts()
            onResult(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
                    .setDebugMessage("ยังไม่พบข้อมูลแพ็กเกจจาก Google Play Console ($productId)")
                    .build()
            )
            return
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        // For subscription products, offerToken is required
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offers = productDetails.subscriptionOfferDetails
            val offerToken = offers?.firstOrNull { it.offerId == null }?.offerToken
                ?: offers?.firstOrNull()?.offerToken

            if (!offerToken.isNullOrBlank()) {
                productDetailsParamsBuilder.setOfferToken(offerToken)
                Log.d(TAG, "Using offerToken for $productId (offers count: ${offers?.size})")
            } else {
                Log.e(TAG, "No active offer details found for subscription: $productId. Offers: $offers")
                onResult(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.DEVELOPER_ERROR)
                        .setDebugMessage("แพ็กเกจ $productId ยังไม่ได้เปิดใช้งาน (Activate) Base Plan ใน Google Play Console")
                        .build()
                )
                return
            }
        }

        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))

        // Support subscription replacement / upgrade / downgrade if active subscription exists
        val currentSub = _activePurchases.value.firstOrNull { it.products.contains(PRODUCT_SUB_MONTHLY) || it.products.contains(PRODUCT_SUB_YEARLY) }
        if (currentSub != null && productDetails.productType == BillingClient.ProductType.SUBS) {
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(currentSub.purchaseToken)
                .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE)
                .build()
            billingFlowParamsBuilder.setSubscriptionUpdateParams(updateParams)
        }

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParamsBuilder.build())
        onResult(billingResult)
    }

    /**
     * Google Play PurchasesUpdatedListener callback
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled billing flow.")
                _billingStatusMessage.value = "ยกเลิกขั้นตอนการสมัคร"
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "User already owns this item. Refreshing purchases...")
                queryPurchases()
                _billingStatusMessage.value = "คุณได้สมัครแพ็กเกจนี้ไว้แล้ว"
            }
            else -> {
                Log.e(TAG, "Billing error: ${billingResult.debugMessage} (code: ${billingResult.responseCode})")
                _billingStatusMessage.value = "เกิดข้อผิดพลาดจาก Google Play: ${billingResult.debugMessage}"
            }
        }
    }

    /**
     * Process a purchase: Acknowledge within 3 days and grant VIP entitlement
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Must acknowledge purchase if not yet acknowledged
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully: ${purchase.orderId}")
                        grantVipEntitlement(purchase)
                    } else {
                        Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            } else {
                grantVipEntitlement(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending completion (e.g. offline cash payment): ${purchase.orderId}")
            _billingStatusMessage.value = "การชำระเงินอยู่ในสถานะรอดำเนินการ (Pending)"
        }
    }

    /**
     * Grant VIP status and bonus stars in local SupabaseDataManager
     */
    private fun grantVipEntitlement(purchase: Purchase) {
        val isLifetime = purchase.products.contains(PRODUCT_LIFETIME)
        val isYearly = purchase.products.contains(PRODUCT_SUB_YEARLY)
        val isMonthly = purchase.products.contains(PRODUCT_SUB_MONTHLY)

        val tierName = when {
            isLifetime -> "ครอบครัวซูเปอร์คิดส์ (ตลอดชีพ)"
            isYearly -> "สมาชิก VIP นักอ่าน (รายปี)"
            isMonthly -> "ผู้สนับสนุนตัวน้อย (รายเดือน)"
            else -> "สมาชิก VIP สนับสนุน"
        }

        val bonusStars = when {
            isLifetime -> 300
            isYearly -> 100
            isMonthly -> 20
            else -> 50
        }

        dataManager.setSubscription(
            tierName = tierName,
            isLifetime = isLifetime,
            bonusStars = bonusStars
        )
        Log.d(TAG, "VIP entitlement granted for tier: $tierName (Lifetime: $isLifetime)")
    }

    /**
     * Open Google Play Subscriptions management page according to Google Play best practices
     */
    fun openManageSubscriptions(activity: Activity, productId: String? = null) {
        try {
            val uri = if (productId != null) {
                Uri.parse("https://play.google.com/store/account/subscriptions?package=${activity.packageName}&sku=$productId")
            } else {
                Uri.parse("https://play.google.com/store/account/subscriptions?package=${activity.packageName}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Play Store subscription management: ${e.message}")
        }
    }

    /**
     * Format price string from Google Play details or fallback to default
     */
    fun getFormattedPrice(productId: String, fallbackPrice: String): String {
        val details = _productDetailsMap.value[productId] ?: return fallbackPrice
        return when (details.productType) {
            BillingClient.ProductType.SUBS -> {
                val pricingPhase = details.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.firstOrNull()
                pricingPhase?.formattedPrice ?: fallbackPrice
            }
            BillingClient.ProductType.INAPP -> {
                details.oneTimePurchaseOfferDetails?.formattedPrice ?: fallbackPrice
            }
            else -> fallbackPrice
        }
    }
}
