package com.pongploydev.priyankvex.paintapp.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.pongploydev.priyankvex.paintapp.utils.AppUpdateCustomsManager
import com.pongploydev.priyankvex.paintapp.utils.RtlViewPager
import com.pongploydev.priyankvex.paintapp.utils.ViewPagerHelper

class MainActivity : AppCompatActivity(), DefaultLifecycleObserver {

    private companion object {
        const val TAG = "MainActivity"
        private const val REQUEST_CODE_UPDATE = 1001
    }

    private lateinit var appUpdateManager: AppUpdateCustomsManager
    private lateinit var playCoreAppUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager
    private lateinit var coordinator: CoordinatorLayout
    private var btnSort: ImageButton? = null
    private var exitTime = 0L
    private lateinit var adView: AdView

    // ตัวแปรสำหรับจัดการโฆษณาสำหรับเด็ก
    private var isChildDirectedApp = true // ตั้งค่าเป็น true สำหรับแอปเด็ก
    private var isGdprApplicable = true // ตั้งค่าเป็น true ถ้าใช้งานใน EU

    // สำหรับติดตามสถานะการอัพเดต
    private var isUpdateInProgress = false
    private var currentUpdateType = AppUpdateType.FLEXIBLE



    val IMMEDIATE: Int = AppUpdateType.IMMEDIATE

    // Listener สำหรับการอัพเดตสถานะการติดตั้ง
    private val installStateListener = InstallStateUpdatedListener { state ->
        handleInstallStateUpdate(state)
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

        // ตั้งค่า AdMob สำหรับแอปเด็ก
        initializeAdMobForChildren()

        adView = findViewById(R.id.adView)

        // coordinator = findViewById(R.id.coordinatorLayout)

        // ตั้งค่า AppUpdateManager
        initializeAppUpdateManager()

        setupToolbarAndNav()
        setupViewPager()
        setupAdMob()

        // ตรวจสอบอัพเดตเมื่อแอปเริ่มต้น
        checkForAppUpdates()
    }

    private fun initializeAppUpdateManager() {
        try {
            // Initialize Play Core AppUpdateManager
            playCoreAppUpdateManager = AppUpdateManagerFactory.create(this)

            // Initialize custom AppUpdateManager
            appUpdateManager = AppUpdateCustomsManager(this)

            // Register install state listener
            playCoreAppUpdateManager.registerListener(installStateListener)

            Log.d(TAG, "AppUpdateManager initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AppUpdateManager", e)
        }
    }

    private fun checkForAppUpdates() {
        Log.d(TAG, "Checking for app updates...")

        // ตรวจสอบอัพเดตแบบ Flexible (สามารถดาวน์โหลดในพื้นหลังได้)
        appUpdateManager.checkForUpdate(currentUpdateType,
            object : AppUpdateCustomsManager.OnUpdateCheckListener {
                override fun onUpdateAvailable(updateInfo: AppUpdateInfo) {
                    Log.d(TAG, "Update available - priority: ${updateInfo.updatePriority()}")
                    showUpdateAvailableDialog(updateInfo)
                }

                override fun onUpdateNotAvailable() {
                    Log.d(TAG, "App is up to date")
                    // สามารถตรวจสอบอัพเดตแบบ Immediate เป็นแผนสำรองได้
                    checkForImmediateUpdate()
                }

                override fun onUpdateCheckFailed(error: String) {
                    Log.e(TAG, "Update check failed: $error")
                    // แสดงข้อความ error หรือลองใหม่ในภายหลัง
                    showUpdateCheckError(error)
                }
            })
    }

    private fun checkForImmediateUpdate() {
        // ตรวจสอบอัพเดตแบบ Immediate เป็นแผนสำรอง
        appUpdateManager.checkForUpdate(IMMEDIATE,
            object : AppUpdateCustomsManager.OnUpdateCheckListener {
                override fun onUpdateAvailable(updateInfo: AppUpdateInfo) {
                    Log.d(TAG, "Immediate update available")
                    // สำหรับ Immediate update สามารถเริ่มได้ทันที
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

    private fun showUpdateAvailableDialog(updateInfo: AppUpdateInfo) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("อัพเดตใหม่มีให้ดาวน์โหลด")
            .setMessage("มีเวอร์ชันใหม่ของแอปพลิเคชัน คุณต้องการอัพเดตตอนนี้หรือไม่?\n\nเวอร์ชันใหม่จะมีประสิทธิภาพที่ดีขึ้นและแก้ไขข้อบกพร่องต่างๆ")
            .setPositiveButton("อัพเดตตอนนี้") { dialog, which ->
                startFlexibleUpdate(updateInfo)
            }
            .setNegativeButton("ภายหลัง") { dialog, which ->
                Toast.makeText(this, "คุณสามารถอัพเดตได้ภายหลังจากการตั้งค่า", Toast.LENGTH_SHORT).show()
                // บันทึกว่าผู้ใช้ปฏิเสธอัพเดต
                setUpdatePostponed()
            }
            .setNeutralButton("อัพเดตโดยไม่ถามอีก") { dialog, which ->
                startFlexibleUpdate(updateInfo)
                setAutoUpdateEnabled(true)
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun startFlexibleUpdate(updateInfo: AppUpdateInfo) {
        try {
            isUpdateInProgress = true
            currentUpdateType = AppUpdateType.FLEXIBLE

            playCoreAppUpdateManager.startUpdateFlowForResult(
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
        try {
            isUpdateInProgress = true
            currentUpdateType = AppUpdateType.IMMEDIATE

            playCoreAppUpdateManager.startUpdateFlowForResult(
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

        // ปิด dialog หลังจาก 3 วินาที (ผู้ใช้สามารถปิดได้ด้วยตนเอง)
        android.os.Handler().postDelayed({
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

                // สามารถอัพเดต progress bar ได้ที่นี่
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

                // Unregister listener หลังจากติดตั้งเสร็จ
                playCoreAppUpdateManager.unregisterListener(installStateListener)
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
        // อัพเดต UI เพื่อแสดงความคืบหน้า (ถ้ามี)
        // เช่น อัพเดต ProgressBar, TextView
        runOnUiThread {
            // อัพเดต UI ตรงนี้
            // progressBar.progress = progress
            // tvProgress.text = "$progress%"
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
        try {
            playCoreAppUpdateManager.completeUpdate()
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
                    // สำหรับ Immediate update, แอปอาจจะต้องรีสตาร์ท
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

    private fun setUpdatePostponed() {
        // บันทึกว่าผู้ใช้ปฏิเสธอัพเดต
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putLong("last_update_postponed", System.currentTimeMillis()).apply()
    }

    private fun setAutoUpdateEnabled(enabled: Boolean) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putBoolean("auto_update_enabled", enabled).apply()
    }

    private fun checkForUpdateInProgress() {
        playCoreAppUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    Log.d(TAG, "Resuming update in progress")
                    // ดำเนินการอัพเดตต่อ
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        startImmediateUpdate(appUpdateInfo)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        startFlexibleUpdate(appUpdateInfo)
                    }
                }
            }
    }

    // เมธอดสำหรับเปิด Play Store สำหรับการอัพเดตแบบ manual
    private fun openPlayStoreForUpdate() {
        try {
            val appPackageName = packageName
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$appPackageName")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val appPackageName = packageName
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            }
            startActivity(intent)
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

        // รีโหลดโฆษณาเมื่อแอปกลับมาทำงานอีกครั้ง
        if (::adView.isInitialized && isChildDirectedApp) {
            adView.resume()
        }

        // ตรวจสอบว่ามีอัพเดตที่กำลังดำเนินการอยู่หรือไม่
        if (::playCoreAppUpdateManager.isInitialized) {
            checkForUpdateInProgress()
        }
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()

        // ล้างทรัพยากร
        if (::adView.isInitialized) {
            adView.destroy()
        }

        if (::playCoreAppUpdateManager.isInitialized ) {
            playCoreAppUpdateManager.unregisterListener(installStateListener)
        }

        if (::appUpdateManager.isInitialized) {
            appUpdateManager.cleanup()
        }
    }

    // ส่วนที่เหลือของโค้ดเดิม (AdMob, UI setup, etc.)
    private fun initializeAdMobForChildren() {
        try {
            // ตั้งค่า RequestConfiguration สำหรับแอปเด็ก
            val configuration = RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(
                    if (isChildDirectedApp) {
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                    } else {
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
                    }
                )
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G) // ระดับ G เท่านั้น
                .build()

            MobileAds.setRequestConfiguration(configuration)

            // ตั้งค่าสำหรับ GDPR (ถ้าจำเป็น)
            if (isGdprApplicable) {
                val consentStatus = getConsentStatus()
                updateAdMobConsent(consentStatus)
            }

            // Initialize Mobile Ads SDK
            MobileAds.initialize(this) {
                Log.d(TAG, "AdMob initialized for child-directed app")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob for children", e)
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
        when (consentStatus) {
            ConsentStatus.PERSONALIZED -> {
                MobileAds.setRequestConfiguration(
                    RequestConfiguration.Builder()
                        .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                        .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                        .build()
                )
            }
            ConsentStatus.NON_PERSONALIZED -> {
                MobileAds.setRequestConfiguration(
                    RequestConfiguration.Builder()
                        .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                        .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                        .build()
                )
            }
            ConsentStatus.UNKNOWN -> {
                handleUnknownConsent()
            }
        }
    }

    private fun handleUnknownConsent() {
        adView.visibility = View.GONE
        Log.w(TAG, "Consent status unknown - ads are hidden")
    }

    private fun setupAdMob() {
        if (!::adView.isInitialized) {
            Log.e(TAG, "AdView is not initialized")
            return
        }

        if (!isChildDirectedApp) {
            Log.w(TAG, "App is not marked as child-directed. Please review COPPA compliance.")
        }

        val adRequest = if (isGdprApplicable && getConsentStatus() == ConsentStatus.NON_PERSONALIZED) {
            AdRequest.Builder()
                .build()
        } else {
            AdRequest.Builder()
                .build()
        }

        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Child-friendly ad loaded successfully")
                adView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Ad failed to load: ${adError.message}")
                adView.visibility = View.GONE
            }

            override fun onAdOpened() {
                Log.d(TAG, "Child-friendly ad opened")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad clicked - monitoring for child safety")
            }

            override fun onAdClosed() {
                Log.d(TAG, "Ad closed")
                if (isChildDirectedApp) {
                    val newAdRequest = AdRequest.Builder().build()
                    adView.loadAd(newAdRequest)
                }
            }

            override fun onAdImpression() {
                Log.d(TAG, "Child-friendly ad impression recorded")
            }
        }
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

        if (::adView.isInitialized) {
            adView.loadAd(AdRequest.Builder().build())
        }
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
        view.visibility = View.VISIBLE
        val anim = TranslateAnimation(0f, 0f, view.height.toFloat(), 0f)
        anim.duration = 300; anim.fillAfter = true
        view.startAnimation(anim)
    }

    private fun slideDown(view: View) {
        val anim = TranslateAnimation(0f, 0f, 0f, view.height.toFloat())
        anim.duration = 300; anim.fillAfter = true
        view.startAnimation(anim)
    }

    override fun onStart(owner: LifecycleOwner) {
        super<AppCompatActivity>.onStart()
    }

    override fun onPause() {
        super<AppCompatActivity>.onPause()
        if (::adView.isInitialized) {
            adView.pause()
        }
    }

    // Enum สำหรับจัดการ consent status
    enum class ConsentStatus {
        PERSONALIZED,
        NON_PERSONALIZED,
        UNKNOWN
    }
}