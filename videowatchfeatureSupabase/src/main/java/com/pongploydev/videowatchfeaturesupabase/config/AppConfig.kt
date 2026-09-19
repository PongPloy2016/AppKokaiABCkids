package com.pongploydev.videowatchfeaturesupabase.config

import com.pongploydev.videowatchfeaturesupabase.Constant


object AppConfig {

    // ==================== API & Supabase Configuration ====================
    const val LOAD_MORE = 10
    const val REST_API_KEY: String = "cda11kfV2SDYTQrvIcAleR75FBh0qN4JjLiZym8sWtOx3oKgbM"
    const val ALLOW_APP_ACCESSED_USING_VPN = true

    // Supabase Configuration
    const val SUPABASE_URL = "https://qriqerrkxccpnwabphdc.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_4pbBttafKQoR_Bj78vtARA_eDMkRe66"


    // ==================== AdMob Configuration ====================

    // ตั้งค่าเปิด/ปิดโฆษณา
    const val ENABLE_ADMOB_BANNER = true
    const val ENABLE_ADMOB_INTERSTITIAL = true
    const val ENABLE_ADMOB_NATIVE = false

    // ตั้งค่า Debug Mode
    const val DEBUG = false // false สำหรับโฆษณาจริง (Production Real Ads)

    // Test Ad Unit IDs (สำหรับ Debug/Development)
    val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // Real Ad Unit IDs (สำหรับ Production - ต้องเปลี่ยนเป็นของตัวเอง)
    // วิธีหา: https://developers.google.com/admob/android/quick-start#get_your_ad_unit_id
    const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111" // เปลี่ยนเป็น ID จริง
    const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-7617756372558825/3832276630" // เปลี่ยนเป็น ID จริง
    const val ADMOB_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110" // เปลี่ยนเป็น ID จริง

    // AdMob App ID (สำคัญ!)
    // หาได้ที่: AdMob Console → Apps → App settings
    const val ADMOB_APP_ID = "ca-app-pub-7617756372558825~2820421176" // เปลี่ยนเป็น App ID จริง

    // Ad Frequency Settings
    const val INTERSTITIAL_AD_FREQUENCY = 3 // แสดงโฆษณาทุก 3 ครั้ง
    const val MIN_TIME_BETWEEN_ADS = 60000L // 60 วินาที (มิลลิวินาที)

    // Ad Loading Timeout
    const val AD_LOAD_TIMEOUT = 10000L // 10 วินาที

    // ==================== Ad Testing Settings ====================

    // Device IDs สำหรับทดสอบ (ใส่ device ID ของคุณ)
    val TEST_DEVICE_IDS = listOf(
        "33BE2250B43518CCDA7DE426D04EE231", // Example test device ID
        "YOUR_DEVICE_ID_HERE" // ใส่ Device ID ของคุณ
    )

    // ==================== Utility Methods ====================

    /**
     * ตรวจสอบว่าเป็น Debug mode หรือไม่
     */
    fun isDebugMode(): Boolean {
        return DEBUG
    }

    /**
     * ดึง Banner Ad Unit ID ตาม mode
     */
    fun getBannerAdUnitId(): String {
        return if (DEBUG) TEST_BANNER_AD_UNIT_ID else ADMOB_BANNER_ID
    }

    /**
     * ดึง Interstitial Ad Unit ID ตาม mode
     */
    fun getInterstitialAdUnitId(): String {
        return if (DEBUG) TEST_INTERSTITIAL_AD_UNIT_ID else ADMOB_INTERSTITIAL_ID
    }

    /**
     * ดึง Native Ad Unit ID ตาม mode
     */
    fun getNativeAdUnitId(): String {
        return if (DEBUG) TEST_NATIVE_AD_UNIT_ID else ADMOB_NATIVE_ID
    }

    /**
     * ตรวจสอบว่าโฆษณาเปิดใช้งานหรือไม่
     */
    fun areAdsEnabled(): Boolean {
        return ENABLE_ADMOB_BANNER || ENABLE_ADMOB_INTERSTITIAL || ENABLE_ADMOB_NATIVE
    }

    /**
     * สร้าง AdRequest.Builder พร้อม device ID สำหรับทดสอบ
     */
    fun createAdRequest(): com.google.android.gms.ads.AdRequest.Builder {
        val builder = com.google.android.gms.ads.AdRequest.Builder()

        if (DEBUG) {
            // เพิ่ม test device IDs
            TEST_DEVICE_IDS.forEach { deviceId ->
                //builder.addTestDevice(deviceId)
            }
        }

        return builder
    }
    //your Server Key obtained from the admin panel

    const val SERVER_KEY: String =
        "WVVoU01HTklUVFpNZVRsM1lqSTFibU5IZUhabFYxSnNaR2sxYW1JeU1IWmpTRlpwWWtkc2Frd3piSFprV0VwbVpHMXNhMXBYT1hwWU1rNXZXVmMxZFZwWGVHWlpXRUozWWtkc2FsbFlVbkJpTWpWS1drWTVhbUl5TUhWalJ6bDFXak5DYzJJemJHdGFXRmwxV2xkU01Wa3lSakJoVnpsMVRHMHhiRnBIYkdoWldFSjM="

    //your Rest API Key obtained from the admin panel

    //Constant.VIDEO_LIST_DEFAULT or Constant.VIDEO_LIST_COMPACT
    @JvmField
    val DEFAULT_VIDEO_VIEW_TYPE: Int = Constant.VIDEO_LIST_DEFAULT

    //Enable it with true value if want to the app will force to display open ads first before start the main menu
    //Longer duration to start the app may occur depending on internet connection or open ad response time itself
    const val FORCE_TO_SHOW_APP_OPEN_AD_ON_START: Boolean = false

    //press back twice to exit from player screen
    const val PRESS_BACK_TWICE_TO_CLOSE_PLAYER: Boolean = false

    //layout customization
    const val SET_CATEGORY_AS_MAIN_MENU: Boolean = false
    const val FORCE_PLAYER_TO_LANDSCAPE: Boolean = false
    const val ENABLE_VIEW_COUNT: Boolean = true
    const val ENABLE_DATE_DISPLAY: Boolean = true
    const val DISPLAY_DATE_AS_TIME_AGO: Boolean = true
    const val ENABLE_VIDEO_COUNT_ON_CATEGORY: Boolean = true

    //highly recommended to keep with false value to prevent the app accessed using VPN for security reason

    //if you use RTL Language e.g : Arabic Language or other, set true
    const val ENABLE_RTL_MODE: Boolean = false

    //display dialog for exit confirmation
    const val ENABLE_EXIT_DIALOG: Boolean = true

    const val ENABLE_GDPR_UMP_SDK: Boolean = true

    //load more for next list videos

    //delay splash screen
    const val DELAY_SPLASH: Int = 100


    const val ENABLE_NEW_APP_DESIGN: Boolean = true

    //show one latest news as header view
    const val SHOW_HEADER_VIEW: Boolean = true

    //show news list in large view
    const val BIG_NEWS_LIST_STYLE: Boolean = false

    //show short description in the news list
    const val SHOW_EXCERPT_IN_POST_LIST: Boolean = true

    //show total news in each category
    const val SHOW_POST_COUNT_IN_CATEGORY: Boolean = false

    //video player orientation
    const val FORCE_VIDEO_PLAYER_TO_LANDSCAPE: Boolean = false

    //date display configuration
    const val SHOW_POST_DATE: Boolean = true
    const val SHOW_DATE_DISPLAY_AS_TIME_AGO: Boolean = false

    //enable view count in the news description
    const val SHOW_POST_VIEW_COUNT: Boolean = true


    //set false to disable copy text in the news description
    const val ENABLE_TEXT_SELECTION: Boolean = false

    //open link in the news description using external web browser
    const val OPEN_LINK_INSIDE_APP: Boolean = true


    const val SET_CATEGORY_AS_MAIN_PAGE: Boolean = false


    //Delay splash screen
    const val DELAY_SPLASH_SCREEN: Int = 1000
}