package com.pongploydev.kokaiabckids.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.compose.FloatingKidsBottomNavBar
import com.pongploydev.kokaiabckids.log.AnalyticsLogger
import com.pongploydev.kokaiabckids.utils.AppUpdateCustomsManager
import com.pongploydev.kokaiabckids.utils.RtlViewPager
import com.pongploydev.kokaiabckids.utils.ViewPagerHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.pongploydev.profilefeature.compose.SubscriptionDialogHelper

class MainActivity : AppCompatActivity(), DefaultLifecycleObserver, NavigationView.OnNavigationItemSelectedListener {

    private companion object {
        const val TAG = "MainActivity"
        private const val REQUEST_CODE_UPDATE = 1001
        private const val AD_STATE_NOT_INITIALIZED = 0
        private const val AD_STATE_INITIALIZED = 1
        private const val AD_STATE_UNIT_ID_SET = 2
        private const val AD_LOAD_DELAY_MS = 30000L // 30 seconds delay for child apps
    }

    // เปลี่ยนเป็น nullable variables
    private var appUpdateManager: AppUpdateCustomsManager? = null
    private var playCoreAppUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var coordinator: CoordinatorLayout
    private var btnSort: ImageButton? = null
    private var exitTime = 0L
    private var adView: AdView? = null
    private var adViewState = AD_STATE_NOT_INITIALIZED
    private var isAdViewInitialized = false
    private var currentAdUnitId: String? = null

    // ตัวแปรสำหรับจัดการโฆษณาสำหรับเด็ก
    private var isChildDirectedApp = true // ✅ ตั้งค่าเป็น true สำหรับแอปเด็ก
    private var isGdprApplicable = true
    private var isAdLoading = false
    private val selectedBottomTabIndex = mutableIntStateOf(0)

    // สำหรับติดตามสถานะการอัพเดต
    private var isUpdateInProgress = false
    private var currentUpdateType = AppUpdateType.FLEXIBLE

    // สร้าง Instance ของ Firebase Analytics
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var analyticsLogger: AnalyticsLogger? = null

    val IMMEDIATE: Int = AppUpdateType.IMMEDIATE

    // Listener สำหรับการอัพเดตสถานะการติดตั้ง
    private val installStateListener = InstallStateUpdatedListener { state ->
        if (isPlayCoreAppUpdateManagerAvailable()) {
            handleInstallStateUpdate(state)
        } else {
            Log.w(TAG, "AppUpdateManager not available - ignoring install state update")
        }
    }

    // Activity Result Launcher สำหรับการอัพเดต
    private val updateActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        handleUpdateResult(result.resultCode)
    }

    // Handler สำหรับจัดการการโหลดโฆษณาแบบดีเลย์
    private val adHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize UI components ก่อน
        initializeUiComponents()

        // 2. Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // 3. Initialize AdView ก่อน (เพียงครั้งเดียว)
        if (!isAdViewInitialized) {
            initializeAdViewWithDefaults()
        }

        // 4. ตั้งค่า AdMob สำหรับเด็กและครอบครัวโดยเฉพาะ
        initializeAdMobForFamily()

        // 5. ตั้งค่า AppUpdateManager
        initializeAppUpdateManager()

        setupToolbarAndNav()
        setupViewPager()

        // 6. ตรวจสอบอัพเดตเมื่อแอปเริ่มต้น (เฉพาะถ้าเปิดใช้งาน)
        if (shouldEnableInAppUpdates()) {
            checkForAppUpdates()
        }

        // 7. เชื่อมต่อ Google Play Billing เพื่อตรวจสอบสิทธิสมาชิก/กู้คืนการซื้ออัตโนมัติ
        try {
            com.pongploydev.profilefeature.billing.PlayBillingManager.getInstance(this).startConnection()
        } catch (e: Exception) {
            Log.e(TAG, "PlayBillingManager init error: ${e.message}")
        }

        // 8. ตั้งค่า Back Press Handler
        handleOnBackPressed()
    }

    private fun initializeUiComponents() {
        // coordinator = findViewById(R.id.coordinatorLayout)
        // btnSort = findViewById(R.id.btn_sort)
    }

    private fun initializeAdViewWithDefaults() {
        adView = findViewById(R.id.adView)

        // ✅ ใน AdMob 23.0.0 ไม่ต้องตั้งค่า adSize ในโค้ด ถ้าตั้งใน XML แล้ว
        // adView?.setAdSize(AdSize.BANNER) // ❌ วิธีเก่าใช้ไม่ได้แล้ว

        isAdViewInitialized = true
        adViewState = AD_STATE_INITIALIZED
        Log.d(TAG, "✅ AdView initialized")
    }

    private fun initializeAppUpdateManager() {
        try {
            // Initialize Play Core AppUpdateManager
            playCoreAppUpdateManager = AppUpdateManagerFactory.create(this)

            // Initialize custom AppUpdateManager
            appUpdateManager = AppUpdateCustomsManager(this)

            // Register install state listener
            playCoreAppUpdateManager?.registerListener(installStateListener)

            Log.d(TAG, "AppUpdateManager initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AppUpdateManager", e)
        }
    }

    private fun shouldEnableInAppUpdates(): Boolean {
        // ตรวจสอบว่าติดตั้งจาก Play Store
        if (!isAppInstalledFromPlayStore()) {
            Log.w(TAG, "In-app updates disabled: Not installed from Play Store")
            return false
        }

        return true
    }

    private fun isAppInstalledFromPlayStore(): Boolean {
        return try {
            val installer = packageManager.getInstallerPackageName(packageName)
            val isFromPlayStore = installer == "com.android.vending"

            if (!isFromPlayStore) {
                Log.w(TAG, "App installed from: $installer (expected: com.android.vending)")
            }

            isFromPlayStore
        } catch (e: Exception) {
            Log.e(TAG, "Error checking install source", e)
            false
        }
    }

    private fun isPlayCoreAppUpdateManagerAvailable(): Boolean {
        return playCoreAppUpdateManager != null
    }

    private fun isCustomAppUpdateManagerAvailable(): Boolean {
        return appUpdateManager != null
    }

    private fun checkForAppUpdates() {
        Log.d(TAG, "Checking for app updates...")

        appUpdateManager?.checkForUpdate(currentUpdateType,
            object : AppUpdateCustomsManager.OnUpdateCheckListener {
                override fun onUpdateAvailable(updateInfo: AppUpdateInfo) {
                    Log.d(TAG, "Update available - priority: ${updateInfo.updatePriority()}")
                }

                override fun onUpdateNotAvailable() {
                    Log.d(TAG, "App is up to date")
                    checkForImmediateUpdate()
                }

                override fun onUpdateCheckFailed(error: String) {
                    Log.e(TAG, "Update check failed: $error")
                    if (error.contains("APP_NOT_OWNED", ignoreCase = true)) {
                        Log.w(TAG, "App not owned error - in-app updates not available")
                    } else {
                        showUpdateCheckError(error)
                    }
                }
            })
    }

    private fun checkForImmediateUpdate() {
        if (!isCustomAppUpdateManagerAvailable()) {
            Log.w(TAG, "AppUpdateManager not available - skipping immediate update check")
            return
        }

        appUpdateManager?.checkForUpdate(IMMEDIATE,
            object : AppUpdateCustomsManager.OnUpdateCheckListener {
                override fun onUpdateAvailable(updateInfo: AppUpdateInfo) {
                    Log.d(TAG, "Immediate update available")
                    startImmediateUpdate(updateInfo)
                }

                override fun onUpdateNotAvailable() {
                    Log.d(TAG, "No immediate update available")
                }

                override fun onUpdateCheckFailed(error: String) {
                    Log.e(TAG, "Immediate update check failed: $error")
                }
            })
    }

    private fun startFlexibleUpdate(updateInfo: AppUpdateInfo) {
        val manager = playCoreAppUpdateManager ?: run {
            Log.e(TAG, "AppUpdateManager is null - cannot start flexible update")
            return
        }

        try {
            isUpdateInProgress = true
            currentUpdateType = AppUpdateType.FLEXIBLE

            manager.startUpdateFlowForResult(
                updateInfo,
                AppUpdateType.FLEXIBLE,
                this,
                REQUEST_CODE_UPDATE
            )

            Log.d(TAG, "Flexible update flow started")
            showUpdateProgressDialog()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting flexible update", e)
            isUpdateInProgress = false
            Toast.makeText(this, "เกิดข้อผิดพลาดในการเริ่มต้นอัพเดต", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startImmediateUpdate(updateInfo: AppUpdateInfo) {
        val manager = playCoreAppUpdateManager ?: run {
            Log.e(TAG, "AppUpdateManager is null - cannot start immediate update")
            return
        }

        try {
            isUpdateInProgress = true
            currentUpdateType = AppUpdateType.IMMEDIATE

            manager.startUpdateFlowForResult(
                updateInfo,
                AppUpdateType.IMMEDIATE,
                this,
                REQUEST_CODE_UPDATE
            )

            Log.d(TAG, "Immediate update flow started")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting immediate update", e)
            isUpdateInProgress = false
            Toast.makeText(this, "เกิดข้อผิดพลาดในการเริ่มต้นอัพเดต", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUpdateProgressDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("กำลังดาวน์โหลดอัพเดต")
            .setMessage("กรุณารอสักครู่...\n\nคุณสามารถใช้แอปพลิเคชันได้ตามปกติในระหว่างการดาวน์โหลด")
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }

    private fun handleInstallStateUpdate(state: InstallState) {
        Log.d(TAG, "Install state updated: ${state.installStatus()}")

        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                Log.d(TAG, "Download progress: $progress%")
                updateDownloadProgress(progress)
            }

            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update downloaded successfully")
                showUpdateReadyDialog()
            }

            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed successfully")
                isUpdateInProgress = false
                Toast.makeText(this, "อัพเดตสำเร็จเรียบร้อย!", Toast.LENGTH_SHORT).show()
                playCoreAppUpdateManager?.unregisterListener(installStateListener)
            }

            InstallStatus.FAILED -> {
                Log.e(TAG, "Update installation failed")
                isUpdateInProgress = false
                Toast.makeText(this, "การอัพเดตล้มเหลว กรุณาลองอีกครั้ง", Toast.LENGTH_SHORT).show()
            }

            InstallStatus.CANCELED -> {
                Log.d(TAG, "Update was canceled by user")
                isUpdateInProgress = false
            }

            else -> {
                Log.d(TAG, "Unknown install status: ${state.installStatus()}")
            }
        }
    }

    private fun updateDownloadProgress(progress: Int) {
        runOnUiThread {
            // อัพเดต UI ตรงนี้
        }
    }

    private fun showUpdateReadyDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("ดาวน์โหลดเสร็จสิ้น")
            .setMessage("อัพเดตดาวน์โหลดเสร็จเรียบร้อยแล้ว! คุณต้องการติดตั้งตอนนี้หรือไม่?\n\nแอปพลิเคชันจะปิดตัวชั่วคราวในระหว่างการติดตั้ง")
            .setPositiveButton("ติดตั้งตอนนี้") { dialog, which ->
                completeUpdate()
            }
            .setNegativeButton("ติดตั้งภายหลัง") { dialog, which ->
                Toast.makeText(this, "คุณสามารถติดตั้งได้จากเมนูตั้งค่า", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun completeUpdate() {
        val manager = playCoreAppUpdateManager ?: run {
            Log.e(TAG, "AppUpdateManager is null - cannot complete update")
            return
        }

        try {
            manager.completeUpdate()
            Log.d(TAG, "Completing update installation")
        } catch (e: Exception) {
            Log.e(TAG, "Error completing update", e)
            Toast.makeText(this, "เกิดข้อผิดพลาดในการติดตั้งอัพเดต", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUpdateResult(resultCode: Int) {
        when (resultCode) {
            RESULT_OK -> {
                Log.d(TAG, "Update flow completed successfully")
                if (currentUpdateType == AppUpdateType.IMMEDIATE) {
                    Toast.makeText(this, "กำลังอัพเดตแอปพลิเคชัน...", Toast.LENGTH_SHORT).show()
                }
            }
            RESULT_CANCELED -> {
                Log.d(TAG, "Update flow was canceled by user")
                isUpdateInProgress = false
                Toast.makeText(this, "การอัพเดตถูกยกเลิก", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.w(TAG, "Update flow failed with result code: $resultCode")
                isUpdateInProgress = false
                Toast.makeText(this, "การอัพเดตล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpdateCheckError(error: String) {
        val errorMessage = when {
            error.contains("network", ignoreCase = true) -> "เกิดข้อผิดพลาดเกี่ยวกับเครือข่าย"
            error.contains("timeout", ignoreCase = true) -> "การเชื่อมต่อหมดเวลา"
            else -> "ไม่สามารถตรวจสอบอัพเดตได้ในขณะนี้"
        }

        Toast.makeText(this, "$errorMessage กรุณาลองอีกครั้งในภายหลัง", Toast.LENGTH_LONG).show()
    }

    private fun checkForUpdateInProgress() {
        val manager = playCoreAppUpdateManager ?: return

        manager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    Log.d(TAG, "Resuming update in progress")
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        startImmediateUpdate(appUpdateInfo)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        startFlexibleUpdate(appUpdateInfo)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get app update info", e)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_UPDATE) {
            handleUpdateResult(resultCode)
        }
    }

    override fun onResume() {
        super<AppCompatActivity>.onResume()

        adView?.resume()

        // ✅ ตรวจสอบการตั้งค่าโฆษณาสำหรับเด็กอีกครั้ง
        ensureChildDirectedSettings()

        if (isPlayCoreAppUpdateManagerAvailable() && shouldEnableInAppUpdates()) {
            checkForUpdateInProgress()
        }

        // อัปเดตสถานะการล็อก Drawer โดยเช็คจากการเข้าสู่ระบบ (เป็นสมาชิก) ไม่ใช่ VIP
        val user = com.pongploydev.profilefeature.auth.SupabaseAuthManager.getInstance(this).getStoredUser()
        val isMember = user != null
        
        if (::drawerLayout.isInitialized) {
            if (isMember) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        updateSubscriptionMenuVisibility()
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()

        adView?.destroy()
        // ✅ ล้าง Handler เพื่อป้องกัน memory leaks
        adHandler.removeCallbacksAndMessages(null)

        playCoreAppUpdateManager?.unregisterListener(installStateListener)
        appUpdateManager?.cleanup()
    }

    // ============================================================
    // 🔥 ส่วนที่แก้ไขแล้ว: การจัดการโฆษณาสำหรับเด็กและครอบครัว
    // ============================================================

    private fun initializeAdMobForFamily() {
        try {
            Log.d(TAG, "🚀 Initializing AdMob for FAMILY with child-safe settings")

            // ✅ ตั้งค่าสำหรับแอปเด็กอย่างเคร่งครัด
            val configuration = RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .build()

            MobileAds.setRequestConfiguration(configuration)

            // ✅ ตั้งค่า Consent เป็น NON_PERSONALIZED สำหรับเด็กโดย default
            if (isGdprApplicable) {
                val consentStatus = getConsentStatus()
                if (consentStatus == ConsentStatus.UNKNOWN) {
                    // สำหรับเด็ก ให้ใช้ non-personalized เป็น default
                    updateAdMobConsent(ConsentStatus.NON_PERSONALIZED)
                } else {
                    updateAdMobConsent(consentStatus)
                }
            }

            MobileAds.initialize(this) { initializationStatus ->
                Log.d(TAG, "✅ AdMob Initialized for Family: ${initializationStatus.adapterStatusMap}")

                runOnUiThread {
                    // เรียก setupFamilySafeAdMob() เท่านั้นครั้งเดียว
                    if (adViewState < AD_STATE_UNIT_ID_SET) {
                        setupFamilySafeAdMob()
                    } else {
                        Log.d(TAG, "AdUnitId already set, loading family-safe ad directly")
                        loadFamilySafeAd()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing AdMob for family", e)
            runOnUiThread {
                // เรียก setupFamilySafeTestAdMob() เท่านั้นครั้งเดียว
                if (adViewState < AD_STATE_UNIT_ID_SET) {
                    setupFamilySafeTestAdMob()
                } else {
                    Log.d(TAG, "AdUnitId already set, loading family-safe ad directly")
                    loadFamilySafeAd()
                }
            }
        }
    }

    private fun setupFamilySafeAdMob() {
        val currentAdView = adView ?: return

        // ✅ ใน AdMob 23.0.0 ไม่ต้องตั้งค่า adSize ในโค้ด ถ้าตั้งใน XML แล้ว
        // ตรวจสอบว่า adUnitId ถูกตั้งค่าแล้วหรือไม่
        if (adViewState >= AD_STATE_UNIT_ID_SET) {
            Log.d(TAG, "AdUnitId already set, loading family-safe ad")
            loadFamilySafeAd()
            return
        }

        try {
            val familyAdUnitId = resources.getString(R.string.banner_ad_unit_id)

            // ✅ ใช้ apply {} เพื่อตั้งค่า properties
            currentAdView.apply {
                // ✅ ตรวจสอบว่า adUnitId ยังไม่ถูกตั้งค่า
                if (adUnitId.isNullOrEmpty()) {
                    Log.d(TAG, "👨‍👩‍👧‍👦 Setting FAMILY-SAFE AdUnitId: $familyAdUnitId")
                    adUnitId = familyAdUnitId
                    this@MainActivity.currentAdUnitId = familyAdUnitId
                    this@MainActivity.adViewState = AD_STATE_UNIT_ID_SET
                } else {
                    Log.d(TAG, "👨‍👩‍👧‍👦 AdUnitId already set to: $adUnitId, using existing")
                    this@MainActivity.currentAdUnitId = adUnitId
                    this@MainActivity.adViewState = AD_STATE_UNIT_ID_SET
                }
            }

            setupFamilySafeAdListener(true)
            loadFamilySafeAd()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up family-safe ad", e)
            // ✅ ไม่เรียก setupFamilySafeTestAdMob() ถ้า adUnitId ถูกตั้งค่าแล้ว
            if (adViewState < AD_STATE_UNIT_ID_SET) {
                setupFamilySafeTestAdMob()
            }
        }
    }

    private fun setupFamilySafeTestAdMob() {
        val currentAdView = adView ?: return

        // ✅ ใน AdMob 23.0.0 ไม่ต้องตั้งค่า adSize ในโค้ด
        // ตรวจสอบว่า adUnitId ถูกตั้งค่าแล้วหรือไม่
        if (adViewState >= AD_STATE_UNIT_ID_SET) {
            Log.d(TAG, "AdUnitId already set, skipping test setup")
            loadFamilySafeAd()
            return
        }

        try {
            val testAdUnitId = "ca-app-pub-3279793802315716/1117791498"

            // ✅ ใช้ apply {} เพื่อตั้งค่า properties
            currentAdView.apply {
                // ✅ ตรวจสอบว่า adUnitId ยังไม่ถูกตั้งค่า
                if (adUnitId.isNullOrEmpty()) {
                    Log.d(TAG, "🧪 Setting FAMILY-SAFE TEST AdUnitId: $testAdUnitId")
                    adUnitId = testAdUnitId
                    this@MainActivity.currentAdUnitId = testAdUnitId
                    this@MainActivity.adViewState = AD_STATE_UNIT_ID_SET
                } else {
                    Log.d(TAG, "🧪 AdUnitId already set to: $adUnitId, using existing")
                    this@MainActivity.currentAdUnitId = adUnitId
                    this@MainActivity.adViewState = AD_STATE_UNIT_ID_SET
                }
            }

            setupFamilySafeAdListener(false)
            loadFamilySafeAd()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up family-safe test ad", e)
            currentAdView.visibility = View.GONE
        }
    }

    private fun setupFamilySafeAdListener(isRealAd: Boolean) {
        val currentAdView = adView ?: return

        currentAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, if (isRealAd) "🎉 FAMILY-SAFE AD LOADED!" else "✅ FAMILY-SAFE TEST AD LOADED")
                currentAdView.visibility = View.GONE // ซ่อน banner ชั่วคราว
                isAdLoading = false

                // ✅ ตรวจสอบเพิ่มเติมว่าโฆษณาปลอดภัยสำหรับเด็ก
                validateAdContentForChildren()

                // ✅ Log event สำหรับ analytics
                logFamilyAdEvent("ad_loaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, if (isRealAd) "❌ FAMILY AD FAILED: ${adError.message}" else "❌ FAMILY TEST AD FAILED: ${adError.message}")
                Log.e(TAG, "Error Code: ${adError.code}, Domain: ${adError.domain}")

                // ✅ สำหรับแอปเด็ก ถ้าโฆษณาโหลด失败 ให้ซ่อนโฆษณาไปเลย
                currentAdView.visibility = View.GONE
                isAdLoading = false

                // ✅ ใน AdMob 23.0.0 ใช้ adError.code ตรวจสอบโดยตรง
                if (adError.code == 3) { // 3 คือ ERROR_CODE_NO_FILL ในเวอร์ชันเก่า
                    Log.w(TAG, "No family-safe ads available - this is expected for child-directed apps")
                }

                // ✅ Log event สำหรับ analytics
                logFamilyAdEvent("ad_failed", adError.toString())
            }

            override fun onAdOpened() {
                Log.d(TAG, "Family-safe ad opened")
                // ✅ Log analytics สำหรับโฆษณาเด็ก
                logFamilyAdEvent("ad_opened")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Family-safe ad clicked")
                // ✅ Log analytics สำหรับการคลิกโฆษณาเด็ก
                logFamilyAdEvent("ad_clicked")
            }

            override fun onAdClosed() {
                Log.d(TAG, "Family-safe ad closed")
                // ✅ สำหรับเด็ก ไม่โหลดโฆษณาใหม่ทันที เพื่อลดการโฆษณาที่มากเกินไป
                adHandler.removeCallbacksAndMessages(null)
                adHandler.postDelayed({
                    if (!isAdLoading) {
                        loadFamilySafeAd()
                    }
                }, AD_LOAD_DELAY_MS) // ⏳ รอ 30 วินาทีก่อนโหลดโฆษณาใหม่

                logFamilyAdEvent("ad_closed")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Family-safe ad impression recorded")
                logFamilyAdEvent("ad_impression")
            }
        }
    }

    private fun loadFamilySafeAd() {
        val currentAdView = adView ?: return

        // ✅ ใช้ apply {} เพื่อตรวจสอบ properties
        currentAdView.apply {
            // ✅ ตรวจสอบว่า adUnitId ถูกตั้งค่าแล้ว
            if (adUnitId.isNullOrEmpty()) {
                Log.e(TAG, "❌ Cannot load ad: AdUnitId is null or empty")
                visibility = View.GONE
                this@MainActivity.isAdLoading = false
                return
            }
        }

        if (isAdLoading) {
            Log.d(TAG, "Ad is already loading, skipping...")
            return
        }

        try {
            isAdLoading = true
            Log.d(TAG, "👨‍👩‍👧‍👦 Loading FAMILY-SAFE ad - UnitId: ${currentAdView.adUnitId}")

            // ✅ ใช้ Request สำหรับเด็กโดยเฉพาะ
            val adRequest = AdRequest.Builder().build()

            currentAdView.loadAd(adRequest)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading family-safe ad", e)
            currentAdView.visibility = View.GONE
            isAdLoading = false
        }
    }

    private fun validateAdContentForChildren() {
        // ✅ ตรวจสอบว่าโฆษณามีขนาดเหมาะสมและไม่มีการโต้ตอบที่หลอกลวง
        val currentAdView = adView ?: return

        // ตรวจสอบว่าโฆษณาไม่ใหญ่เกินไป (ไม่เกิน 15% ของหน้าจอ)
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val adHeight = currentAdView.height

        if (adHeight > screenHeight * 0.15) {
            Log.w(TAG, "⚠️ Ad might be too large for child-directed app - hiding")
            currentAdView.visibility = View.GONE
            return
        }

        Log.d(TAG, "✅ Ad content validated for children - ad height: $adHeight, screen height: $screenHeight")
    }

    private fun ensureChildDirectedSettings() {
        // ✅ ยืนยันการตั้งค่าโฆษณาสำหรับเด็กทุกครั้งที่กลับมาใช้แอป
        val configuration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
            .build()

        MobileAds.setRequestConfiguration(configuration)
        Log.d(TAG, "✅ Child-directed settings verified")
    }

    private fun logFamilyAdEvent(event: String, errorMessage: String? = null) {
        try {
            val params = Bundle().apply {
                putString("ad_unit_id", currentAdUnitId ?: "unknown")
                putString("ad_event", event)
                putBoolean("child_directed", true)
                putBoolean("family_safe", true)
                errorMessage?.let { putString("error_message", it) }
            }
            firebaseAnalytics.logEvent("family_ad_$event", params)
            Log.d(TAG, "📊 Logged family ad event: $event")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging family ad event", e)
        }
    }

    private fun getConsentStatus(): ConsentStatus {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return when (sharedPref.getString("consent_status", "UNKNOWN")) {
            "PERSONALIZED" -> ConsentStatus.PERSONALIZED
            "NON_PERSONALIZED" -> ConsentStatus.NON_PERSONALIZED
            else -> ConsentStatus.UNKNOWN
        }
    }

    private fun updateAdMobConsent(consentStatus: ConsentStatus) {
        val childDirectedTag = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
        val adContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G

        // ✅ สำหรับแอปเด็ก ควรใช้ non-personalized ads เป็นหลัก
        val finalConsentStatus = if (consentStatus == ConsentStatus.UNKNOWN) {
            ConsentStatus.NON_PERSONALIZED
        } else {
            consentStatus
        }

        val configurationBuilder = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(childDirectedTag)
            .setMaxAdContentRating(adContentRating)

        MobileAds.setRequestConfiguration(configurationBuilder.build())

        // บันทึก consent status
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putString("consent_status",
            when (finalConsentStatus) {
                ConsentStatus.PERSONALIZED -> "PERSONALIZED"
                ConsentStatus.NON_PERSONALIZED -> "NON_PERSONALIZED"
                else -> "NON_PERSONALIZED"
            }
        ).apply()

        Log.d(TAG, "✅ Consent updated for family app: $finalConsentStatus")

        // ✅ ตรวจสอบการตั้งค่า AdView ก่อนโหลดโฆษณา
        if (validateAdViewSettings()) {
            adHandler.postDelayed({
                loadFamilySafeAd()
            }, 1000)
        } else {
            Log.e(TAG, "❌ Cannot load ad due to invalid AdView settings")
        }
    }

    private fun validateAdViewSettings(): Boolean {
        val currentAdView = adView ?: return false

        return when {
            !isAdViewInitialized -> {
                Log.e(TAG, "❌ AdView not initialized")
                false
            }
            currentAdView.adUnitId.isNullOrEmpty() -> {
                Log.e(TAG, "❌ AdUnitId is null or empty")
                false
            }
            else -> {
                Log.d(TAG, "✅ AdView settings valid - UnitId: ${currentAdView.adUnitId}")
                true
            }
        }
    }

    private fun handleUnknownConsent() {
        adView?.visibility = View.GONE
        Log.w(TAG, "Consent status unknown - ads are hidden for child safety")
    }

    fun setChildDirectedTreatment(isChildDirected: Boolean) {
        this.isChildDirectedApp = isChildDirected

        val configuration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(
                if (isChildDirected) {
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                } else {
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
                }
            )
            .setMaxAdContentRating(
                if (isChildDirected) {
                    RequestConfiguration.MAX_AD_CONTENT_RATING_G
                } else {
                    RequestConfiguration.MAX_AD_CONTENT_RATING_MA
                }
            )
            .setTagForUnderAgeOfConsent(
                if (isChildDirected) {
                    RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
                } else {
                    RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE
                }
            )
            .build()

        MobileAds.setRequestConfiguration(configuration)

        // โหลด ad ใหม่เมื่อการตั้งค่าเปลี่ยน
        adHandler.postDelayed({
            loadFamilySafeAd()
        }, 1000)

        Log.d(TAG, "✅ Child directed treatment updated: $isChildDirected")
    }

    fun updateGdprConsent(consentGiven: Boolean) {
        // ✅ สำหรับแอปเด็ก แนะนำให้ใช้ non-personalized ads เสมอ
        val consentStatus = if (consentGiven && !isChildDirectedApp) {
            ConsentStatus.PERSONALIZED
        } else {
            ConsentStatus.NON_PERSONALIZED
        }

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putString("consent_status",
            if (consentStatus == ConsentStatus.PERSONALIZED) "PERSONALIZED" else "NON_PERSONALIZED"
        ).apply()

        updateAdMobConsent(consentStatus)
        Log.d(TAG, "✅ GDPR consent updated for family app: $consentStatus")
    }

    private fun setupToolbarAndNav() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(
            this,
            R.color.color_light_primary
        ))

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Override การคลิกที่ปุ่มแฮมเบอร์เกอร์ (เช็คว่าเป็นสมาชิก/เข้าสู่ระบบแล้วหรือยัง)
        toolbar.setNavigationOnClickListener {
            val user = com.pongploydev.profilefeature.auth.SupabaseAuthManager.getInstance(this@MainActivity).getStoredUser()
            if (user != null) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                Toast.makeText(this@MainActivity, "กรุณาเข้าสู่ระบบเพื่อใช้งานเมนูนี้", Toast.LENGTH_SHORT).show()
                // สามารถใส่โค้ดให้เปิดหน้าต่างล็อกอินที่นี่ได้หากต้องการ
            }
        }

        // กำหนดข้อมูลผู้ใช้ใน Nav Header (ถ้ามีการล็อกอิน)
        try {
            val headerView = navigationView.getHeaderView(0)
            val headerTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
            val headerSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
            val supabaseAuthManager = com.pongploydev.profilefeature.auth.SupabaseAuthManager.getInstance(this)
            val user = supabaseAuthManager.getStoredUser()
            if (user != null) {
                val displayName = user.userMetadata?.displayName
                headerTitle.text = if (!displayName.isNullOrBlank()) displayName else "คนเก่ง"
                headerSubtitle.text = if (!user.email.isNullOrBlank()) user.email else "เรียนรู้สนุก ทุกวัน ⭐"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring nav header user info", e)
        }

        val nav = findViewById<BottomNavigationView>(R.id.navigation)
        nav.apply {
            menu.clear()
            inflateMenu(R.menu.bottom_nav_menu)
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        }

        findViewById<ComposeView>(R.id.floating_bottom_nav)?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FloatingKidsBottomNavBar(
                    selectedIndex = selectedBottomTabIndex.intValue,
                    onTabSelected = { index, menuId ->
                        selectedBottomTabIndex.intValue = index
                        findViewById<ViewPager>(R.id.viewpager)?.currentItem = index
                        findViewById<RtlViewPager>(R.id.viewpager_rtl)?.currentItem = index
                        nav.selectedItemId = menuId
                    }
                )
            }
        }

        updateSubscriptionMenuVisibility()
    }

    fun openNewsTab() {
        selectedBottomTabIndex.intValue = 1
        findViewById<ViewPager>(R.id.viewpager)?.currentItem = 1
        findViewById<RtlViewPager>(R.id.viewpager_rtl)?.currentItem = 1
        findViewById<BottomNavigationView>(R.id.navigation)?.selectedItemId = R.id.navigation_news
    }

    fun openVideoTab() {
        selectedBottomTabIndex.intValue = 2
        findViewById<ViewPager>(R.id.viewpager)?.currentItem = 2
        findViewById<RtlViewPager>(R.id.viewpager_rtl)?.currentItem = 2
        findViewById<BottomNavigationView>(R.id.navigation)?.selectedItemId = R.id.navigation_video
    }

    fun openDrawer() {
        val user = com.pongploydev.profilefeature.auth.SupabaseAuthManager.getInstance(this).getStoredUser()
        if (user == null) {
            Toast.makeText(this, "กรุณาเข้าสู่ระบบเพื่อใช้งานเมนูนี้", Toast.LENGTH_SHORT).show()
            return
        }
        if (::drawerLayout.isInitialized) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    fun closeDrawer() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    fun openFavoriteListScreen() {
        val favoriteFragment = com.pongploydev.videowatchfeaturesupabase.FavoriteListFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, favoriteFragment, "FavoriteListFragment")
            .addToBackStack("FavoriteListFragment")
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val vp = findViewById<ViewPager>(R.id.viewpager)
        val nav = findViewById<BottomNavigationView>(R.id.navigation)
        when (item.itemId) {
            R.id.nav_home -> {
                vp.currentItem = 0
                nav.selectedItemId = R.id.navigation_home
            }
            R.id.nav_learn_write -> {
                startActivity(Intent(this, com.pongploydev.kokaiabckids.activity.thailetters.MenuThaiLettersActivity::class.java).apply {
                    putExtra(com.pongploydev.kokaiabckids.activity.thailetters.MenuThaiLettersActivity.EXTRA_MODE, "THAI")
                })
            }
            R.id.nav_learn_english -> {
                startActivity(Intent(this, com.pongploydev.kokaiabckids.activity.thailetters.MenuThaiLettersActivity::class.java).apply {
                    putExtra(com.pongploydev.kokaiabckids.activity.thailetters.MenuThaiLettersActivity.EXTRA_MODE, "ENGLISH")
                })
            }
            R.id.nav_learn_read -> {
                startActivity(Intent(this, com.pongploydev.kokaiabckids.activity.learning.LeaningActvity::class.java))
            }
            R.id.nav_coloring -> {
                startActivity(Intent(this, com.pongploydev.kokaiabckids.activity.learning.showKids.ColoringGameActivity::class.java))
            }
            R.id.nav_game_hub -> {
                vp.currentItem = 4
                nav.selectedItemId = R.id.navigation_game
            }
            R.id.nav_abc_match -> {
                startActivity(Intent(this, com.pongploydev.kokaiabckids.activity.AbcMatchGameActivity::class.java))
            }
            R.id.nav_live -> {
                startActivity(Intent(this, com.pongploydev.livefeature.LiveListActivity::class.java))
            }
            R.id.nav_video -> {
                vp.currentItem = 2
                nav.selectedItemId = R.id.navigation_video
            }
            R.id.nav_favorite -> {
                openFavoriteListScreen()
            }
            R.id.nav_quiz -> {
                vp.currentItem = 3
                nav.selectedItemId = R.id.navigation_quiz
            }
            R.id.nav_leaderboard -> {
                startActivity(Intent(this, com.pongploydev.kokaiabckids.activity.QuizLeaderboardActivity::class.java))
            }
            R.id.nav_news -> {
                vp.currentItem = 1
                nav.selectedItemId = R.id.navigation_news
            }
            R.id.nav_web -> {
                startActivity(com.pongploydev.kidswebviewfeature.KidsWebViewActivity.createIntent(this))
            }
            R.id.nav_profile -> {
                vp.currentItem = 5
                nav.selectedItemId = R.id.navigation_profile
            }
            R.id.nav_subscription -> {
                val dataManager = com.pongploydev.profilefeature.data.SupabaseDataManager.getInstance(this)
                if (dataManager.getCachedAppSettings()?.isSubscriptionEnabled == false) {
                    Toast.makeText(this, "ระบบสมาชิกปิดปรับปรุงชั่วคราว", Toast.LENGTH_SHORT).show()
                } else {
                    showSubscriptionDialog()
                }
            }
            R.id.nav_settings -> {
                showSettingsDialog()
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_about -> {
                showAboutDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateSubscriptionMenuVisibility() {
        try {
            if (!::navigationView.isInitialized) return
            val dataManager = com.pongploydev.profilefeature.data.SupabaseDataManager.getInstance(this)
            val cached = dataManager.getCachedAppSettings()
            val isEnabled = cached?.isSubscriptionEnabled ?: false
            navigationView.menu.findItem(R.id.nav_subscription)?.isVisible = isEnabled

            lifecycleScope.launch {
                dataManager.fetchAppSettings().onSuccess { settings ->
                    if (::navigationView.isInitialized) {
                        navigationView.menu.findItem(R.id.nav_subscription)?.isVisible = settings.isSubscriptionEnabled
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subscription menu visibility", e)
        }
    }

    fun showSubscriptionDialog() {
        val dataManager = com.pongploydev.profilefeature.data.SupabaseDataManager.getInstance(this)
        if (dataManager.getCachedAppSettings()?.isSubscriptionEnabled == false) {
            Toast.makeText(this, "ระบบสมาชิกปิดปรับปรุงชั่วคราว", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = android.app.Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            setContent {
                SubscriptionDialogHelper.Show(
                    onDismiss = { dialog.dismiss() }
                )
            }
        }
        dialog.setContentView(composeView)
        dialog.show()
    }

    private fun shareApp() {
        val appName = getString(R.string.app_name)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, appName)
            putExtra(Intent.EXTRA_TEXT, "ชวนมาสนุกกับแอป $appName ฝึกเขียน ก-ฮ ABC และระบายสีกันนะ! 🎨\nดาวน์โหลดได้ที่: https://play.google.com/store/apps/details?id=$packageName")
        }
        startActivity(Intent.createChooser(intent, "แชร์แอปพลิเคชัน"))
    }

    private fun showAboutDialog() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
        val appName = getString(R.string.app_name)
        val privacyUrl = com.pongploydev.profilefeature.data.SupabaseDataManager.getInstance(this)
            .getCachedAppSettings()?.privacyPolicyUrl
            ?.takeIf { it.isNotBlank() }
            ?: "https://pongploydev.github.io/privacy-policy/"
        AlertDialog.Builder(this)
            .setTitle(appName)
            .setMessage("แอปพลิเคชันเพื่อการเรียนรู้สำหรับเด็ก ฝึกเขียน ก ไก่, ABC, ระบายสี, มินิเกม และสื่อวิดีโอ\nเวอร์ชัน: $versionName\nพัฒนาขึ้นเพื่อเป็นสื่อการเรียนรู้สำหรับเด็กปฐมวัย")
            .setIcon(R.drawable.app)
            .setNeutralButton("นโยบายความเป็นส่วนตัว") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
                } catch (e: Exception) {
                    Toast.makeText(this, "ไม่สามารถเปิดลิงก์นโยบายความเป็นส่วนตัวได้", Toast.LENGTH_SHORT).show()
                }
            }
            .setPositiveButton("ตกลง", null)
            .show()
    }

    private fun showSettingsDialog() {
        val musicManager = com.pongploydev.kokaiabckids.utils.MusicManager.getInstance()
        val isMusicOn = musicManager.isMusicEnabled(this)
        val options = arrayOf(
            if (isMusicOn) "เสียงเพลงพื้นหลัง (เปิดอยู่ - แตะเพื่อปิด)" else "เสียงเพลงพื้นหลัง (ปิดอยู่ - แตะเพื่อเปิด)"
        )
        AlertDialog.Builder(this)
            .setTitle("การตั้งค่า")
            .setItems(options) { _, which ->
                if (which == 0) {
                    musicManager.setMusicEnabled(this, !isMusicOn)
                    Toast.makeText(this, if (!isMusicOn) "เปิดเพลงแล้ว 🎵" else "ปิดเพลงแล้ว 🔇", Toast.LENGTH_SHORT).show()
                }
            }
            .setPositiveButton("ปิด", null)
            .show()
    }

    private fun setupViewPager() {
        val vp = findViewById<ViewPager>(R.id.viewpager)
        val vpRTL = findViewById<RtlViewPager>(R.id.viewpager_rtl)
        val appBarLayout = findViewById<View>(R.id.appbarLayout)
        val helper = ViewPagerHelper(this)
        
        helper.setupViewPagerRtlDefault(vpRTL, findViewById(R.id.navigation), findViewById(R.id.title_toolbar))
        helper.setupViewPagerDefault(vp, findViewById(R.id.navigation), findViewById(R.id.title_toolbar))

        // ฟังก์ชันช่วยจัดการความคล่องตัวของ Toolbar
        val updateToolbarVisibility = { position: Int ->
            // ซ่อน Toolbar สำหรับหน้า Home (0), Web (1), Video (2), Live (4), Profile (5) เพราะมี Toolbar/Header ภายในตัวเองอยู่แล้ว
            if (position == 0 || position == 1 || position == 2 || position == 4 || position == 5) {
                appBarLayout.visibility = View.GONE
            } else {
                appBarLayout.visibility = View.VISIBLE
            }
        }


        // ตั้งค่าเริ่มต้นตามหน้าแรก
        selectedBottomTabIndex.intValue = vp.currentItem
        updateToolbarVisibility(vp.currentItem)
        AnalyticsLogger.logScreenView(this, "หน้าแรก (Home)", "MainGridFragment")

        // ซ่อน Toolbar เมื่อเปลี่ยนหน้า และส่ง Analytics Screen View
        vp.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                selectedBottomTabIndex.intValue = position
                updateToolbarVisibility(position)
                val (screenName, screenClass) = when (position) {
                    0 -> Pair("หน้าแรก (Home)", "MainGridFragment")
                    1 -> Pair("หน้าข่าวสาร (News)", "NewsComposeFragment")
                    2 -> Pair("หน้าวิดีโอ (Video)", "VideoListFragment")
                    3 -> Pair("หน้าแบบทดสอบ (Quiz)", "QuizFragment")
                    4 -> Pair("หน้าเกมส์ (Game Hub)", "GameHubComposeFragment")
                    5 -> Pair("หน้าโปรไฟล์ (Profile)", "ProfileComposeFragment")
                    else -> Pair("หน้าหลัก_$position", "MainActivity")
                }
                AnalyticsLogger.logScreenView(this@MainActivity, screenName, screenClass)
            }
        })

        // สำหรับ RTL ViewPager
        vpRTL.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                selectedBottomTabIndex.intValue = position
                updateToolbarVisibility(position)
                val (screenName, screenClass) = when (position) {
                    0 -> Pair("หน้าแรก (Home)", "MainGridFragment")
                    1 -> Pair("หน้าข่าวสาร (News)", "NewsComposeFragment")
                    2 -> Pair("หน้าวิดีโอ (Video)", "VideoListFragment")
                    3 -> Pair("หน้าแบบทดสอบ (Quiz)", "QuizFragment")
                    4 -> Pair("หน้าเกมส์ (Game Hub)", "GameHubComposeFragment")
                    5 -> Pair("หน้าโปรไฟล์ (Profile)", "ProfileComposeFragment")
                    else -> Pair("หน้าหลัก_$position", "MainActivity")
                }
                AnalyticsLogger.logScreenView(this@MainActivity, screenName, screenClass)
            }
        })

        adView?.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            val extraBottom = if (view.visibility == View.VISIBLE) view.height else 0
            listOf(vp, vpRTL).forEach { pager ->
                val lp = pager.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                if (lp != null && lp.bottomMargin != extraBottom) {
                    lp.bottomMargin = extraBottom
                    pager.layoutParams = lp
                }
            }
        }

        vp.currentItem = 0
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    // Original back behavior or finish
                    finish()
                }
            }
        })
    }

    private fun slideUp(view: View) {
        if (view != null) {
            view.visibility = View.VISIBLE
            val anim = TranslateAnimation(0f, 0f, view.height.toFloat(), 0f)
            anim.duration = 300; anim.fillAfter = true
            view.startAnimation(anim)
        }
    }

    private fun slideDown(view: View) {
        if (view != null) {
            val anim = TranslateAnimation(0f, 0f, 0f, view.height.toFloat())
            anim.duration = 300; anim.fillAfter = true
            view.startAnimation(anim)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super<AppCompatActivity>.onStart()
    }

    override fun onPause() {
        super<AppCompatActivity>.onPause()
        adView?.pause()
        // ✅ ล้าง pending ad loads เมื่อแอปถูก pause
        adHandler.removeCallbacksAndMessages(null)
        isAdLoading = false
    }

    // Enum สำหรับจัดการ consent status
    enum class ConsentStatus {
        PERSONALIZED,
        NON_PERSONALIZED,
        UNKNOWN
    }
}