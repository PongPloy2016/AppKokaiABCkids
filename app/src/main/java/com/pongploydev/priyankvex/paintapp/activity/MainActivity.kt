package com.pongploydev.priyankvex.paintapp.activity

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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
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
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.log.AnalyticsLogger
import com.pongploydev.priyankvex.paintapp.utils.AppUpdateCustomsManager
import com.pongploydev.priyankvex.paintapp.utils.RtlViewPager
import com.pongploydev.priyankvex.paintapp.utils.ViewPagerHelper
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity(), DefaultLifecycleObserver {

    private companion object {
        const val TAG = "MainActivity"
        private const val REQUEST_CODE_UPDATE = 1001
        private const val AD_STATE_NOT_INITIALIZED = 0
        private const val AD_STATE_INITIALIZED = 1
        private const val AD_STATE_UNIT_ID_SET = 2
    }

    // เปลี่ยนเป็น nullable variables
    private var appUpdateManager: AppUpdateCustomsManager? = null
    private var playCoreAppUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager? = null

    private lateinit var coordinator: CoordinatorLayout
    private var btnSort: ImageButton? = null
    private var exitTime = 0L
    private var adView: AdView? = null
    private var adViewState = AD_STATE_NOT_INITIALIZED
    private var isAdViewInitialized = false
    private var currentAdUnitId: String? = null

    // ตัวแปรสำหรับจัดการโฆษณาสำหรับเด็ก
    private var isChildDirectedApp = true
    private var isGdprApplicable = true

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize UI components ก่อน
        initializeUiComponents()

        // 2. Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // 3. Initialize AdView ก่อน (เพียงครั้งเดียว)
        if (!isAdViewInitialized) {
            adView = findViewById(R.id.adView)
            isAdViewInitialized = true
            adViewState = AD_STATE_INITIALIZED
            Log.d(TAG, "AdView initialized")
        }

        // 4. ตั้งค่า AdMob (จะเลือกใช้ real หรือ test อัตโนมัติ)
        initializeAdMobForChildren()

        // 5. ตั้งค่า AppUpdateManager
        initializeAppUpdateManager()

        setupToolbarAndNav()
        setupViewPager()

        // 6. ตรวจสอบอัพเดตเมื่อแอปเริ่มต้น (เฉพาะถ้าเปิดใช้งาน)
        if (shouldEnableInAppUpdates()) {
            checkForAppUpdates()
        }

        // 7. ตั้งค่า Back Press Handler
        handleOnBackPressed()
    }

    private fun initializeUiComponents() {
        //coordinator = findViewById(R.id.coordinatorLayout)
        // btnSort = findViewById(R.id.btn_sort)
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

        if (isPlayCoreAppUpdateManagerAvailable() && shouldEnableInAppUpdates()) {
            checkForUpdateInProgress()
        }
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()

        adView?.destroy()

        playCoreAppUpdateManager?.unregisterListener(installStateListener)
        appUpdateManager?.cleanup()
    }

    // ส่วนของการจัดการ AdMob (แก้ไขแล้ว)
    private fun initializeAdMobForChildren() {
        try {
            Log.d(TAG, "🚀 Initializing AdMob for children...")

            val configuration = RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(
                    if (isChildDirectedApp) {
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                    } else {
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
                    }
                )
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build()

            MobileAds.setRequestConfiguration(configuration)

            if (isGdprApplicable) {
                val consentStatus = getConsentStatus()
                updateAdMobConsent(consentStatus)
            }

            MobileAds.initialize(this) { initializationStatus ->
                Log.d(TAG, "✅ AdMob Initialization Status: ${initializationStatus.adapterStatusMap}")

                runOnUiThread {
                    // เรียก setupAdMobReal() เท่านั้นครั้งเดียว
                    if (adViewState < AD_STATE_UNIT_ID_SET) {
                        setupAdMobReal()
                    } else {
                        Log.d(TAG, "AdUnitId already set, loading ad directly")
                        loadAdWithExistingAdUnitId()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing AdMob", e)
            runOnUiThread {
                // เรียก setupAdMobTest() เท่านั้นครั้งเดียว
                if (adViewState < AD_STATE_UNIT_ID_SET) {
                    setupAdMobTest()
                } else {
                    Log.d(TAG, "AdUnitId already set, loading ad directly")
                    loadAdWithExistingAdUnitId()
                }
            }
        }
    }

    private fun setupAdMobReal() {
        val currentAdView = adView ?: return

        if (adViewState >= AD_STATE_UNIT_ID_SET) {
            Log.d(TAG, "AdUnitId already set, skipping real ad setup")
            loadAdWithExistingAdUnitId()
            return
        }

        try {
            val realAdUnitId = resources.getString(R.string.banner_ad_unit_id)
            Log.d(TAG, "🔄 Setting REAL AdUnitId: $realAdUnitId")

            currentAdView.adUnitId = realAdUnitId
            currentAdUnitId = realAdUnitId
            adViewState = AD_STATE_UNIT_ID_SET

            setupAdListener(true)
            loadAdWithExistingAdUnitId()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up real ad", e)
            // ไม่เรียก setupAdMobTest() อัตโนมัติที่นี่เพื่อป้องกัน loop
        }
    }

    private fun setupAdMobTest() {
        val currentAdView = adView ?: return

        if (adViewState >= AD_STATE_UNIT_ID_SET) {
            Log.d(TAG, "AdUnitId already set, skipping test ad setup")
            loadAdWithExistingAdUnitId()
            return
        }

        try {
            val testAdUnitId = "ca-app-pub-3940256099942544/6300978111"
            Log.d(TAG, "🧪 Setting TEST AdUnitId: $testAdUnitId")

            currentAdView.adUnitId = testAdUnitId
            currentAdUnitId = testAdUnitId
            adViewState = AD_STATE_UNIT_ID_SET

            setupAdListener(false)
            loadAdWithExistingAdUnitId()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up test ad", e)
        }
    }

    private fun setupAdListener(isRealAd: Boolean) {
        val currentAdView = adView ?: return

        currentAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, if (isRealAd) "🎉 REAL AD LOADED SUCCESSFULLY!" else "✅ TEST AD LOADED")
                currentAdView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, if (isRealAd) "❌ REAL AD FAILED: ${adError.message}" else "❌ TEST AD FAILED: ${adError.message}")
                Log.e(TAG, "Error Code: ${adError.code}")

                if (isRealAd) {
                    // สำหรับ real ad ที่ fail ให้ลองใช้ test ad (เฉพาะถ้ายังไม่ได้ตั้งค่า)
                    if (adViewState < AD_STATE_UNIT_ID_SET) {
                        Log.w(TAG, "🔄 Falling back to TEST ads due to error")
                        setupAdMobTest()
                    } else {
                        currentAdView.visibility = View.GONE
                    }
                } else {
                    currentAdView.visibility = View.GONE
                }
            }

            override fun onAdOpened() {
                Log.d(TAG, "Ad opened")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad clicked")
            }

            override fun onAdClosed() {
                Log.d(TAG, "Ad closed")
                // โหลด ad ใหม่เมื่อปิด ad
                Handler(Looper.getMainLooper()).postDelayed({
                    loadAdWithExistingAdUnitId()
                }, 1000)
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad impression recorded")
            }
        }
    }

    // โหลด ad ด้วย adUnitId ที่มีอยู่แล้ว
    private fun loadAdWithExistingAdUnitId() {
        val currentAdView = adView ?: return

        try {
            val adRequest = AdRequest.Builder().build()
            currentAdView.loadAd(adRequest)
            Log.d(TAG, "🔄 Loading ad with existing AdUnitId: $currentAdUnitId")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad with existing AdUnitId", e)
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

        when (consentStatus) {
            ConsentStatus.PERSONALIZED -> {
                // ไม่ต้องทำอะไรพิเศษ
            }
            ConsentStatus.NON_PERSONALIZED -> {
                // ไม่ต้องทำอะไรพิเศษ
            }
            ConsentStatus.UNKNOWN -> {
                handleUnknownConsent()
                return
            }
        }

        val configurationBuilder = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(childDirectedTag)
            .setMaxAdContentRating(adContentRating)

        MobileAds.setRequestConfiguration(configurationBuilder.build())

        // โหลด ad ใหม่เมื่อ consent เปลี่ยน
        loadAdWithExistingAdUnitId()
    }

    private fun handleUnknownConsent() {
        adView?.visibility = View.GONE
        Log.w(TAG, "Consent status unknown - ads are hidden")
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
            .build()

        MobileAds.setRequestConfiguration(configuration)

        // โหลด ad ใหม่เมื่อการตั้งค่าเปลี่ยน
        loadAdWithExistingAdUnitId()
    }

    fun updateGdprConsent(consentGiven: Boolean) {
        val consentStatus = if (consentGiven) {
            ConsentStatus.PERSONALIZED
        } else {
            ConsentStatus.NON_PERSONALIZED
        }

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putString("consent_status",
            if (consentGiven) "PERSONALIZED" else "NON_PERSONALIZED"
        ).apply()

        updateAdMobConsent(consentStatus)
    }

    private fun setupToolbarAndNav() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(
            this,
            R.color.color_light_primary
        ))
        toolbar.setNavigationOnClickListener {  }

        val nav = findViewById<BottomNavigationView>(R.id.navigation)
        nav.apply {
            menu.clear()
            inflateMenu(R.menu.bottom_nav_menu)
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            setBackgroundColor(ContextCompat.getColor(
                this@MainActivity,
                R.color.color_light_bottom_navigation
            ))
        }
    }

    private fun setupViewPager() {
        val vp = findViewById<ViewPager>(R.id.viewpager)
        val vpRTL = findViewById<RtlViewPager>(R.id.viewpager_rtl)
        val helper = ViewPagerHelper(this)
        helper.setupViewPagerRtlDefault(vpRTL, findViewById(R.id.navigation), findViewById(R.id.title_toolbar))
        helper.setupViewPagerDefault(vp, findViewById(R.id.navigation), findViewById(R.id.title_toolbar))

        vp.currentItem = 0
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //exitApp()
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
    }

    // Enum สำหรับจัดการ consent status
    enum class ConsentStatus {
        PERSONALIZED,
        NON_PERSONALIZED,
        UNKNOWN
    }
}