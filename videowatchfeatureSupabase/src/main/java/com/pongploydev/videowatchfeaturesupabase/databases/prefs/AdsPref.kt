package com.pongploydev.videowatchfeaturesupabase.databases.prefs

import android.content.Context
import android.content.SharedPreferences

class AdsPref(context: Context) {
    var context: Context?
    var sharedPreferences: SharedPreferences
    var editor: SharedPreferences.Editor

    // Keys สำหรับ SharedPreferences
    private val PREF_ADS = "ads_pref"

    // Ad Status Keys
    private  val IS_AD_ENABLED = "IS_AD_ENABLED"
    private  val IS_AD_NETWORK_ENABLED = "IS_AD_NETWORK_ENABLED"
    private  val AD_NETWORK_TYPE = "AD_NETWORK_TYPE"

    // Banner Ads Keys
    private  val IS_BANNER_AD_ENABLED = "IS_BANNER_AD_ENABLED"
    private  val BANNER_AD_ID = "BANNER_AD_ID"
    private  val IS_BANNER_HOME = "IS_BANNER_HOME"
    private  val BANNER_HOME_ID = "BANNER_HOME_ID"
    private  val IS_BANNER_POST_DETAILS = "IS_BANNER_POST_DETAILS"
    private  val BANNER_POST_DETAIL_ID = "BANNER_POST_DETAIL_ID"
    private  val IS_BANNER_CATEGORY = "IS_BANNER_CATEGORY"
    private  val BANNER_CATEGORY_ID = "BANNER_CATEGORY_ID"
    private  val IS_BANNER_SEARCH = "IS_BANNER_SEARCH"
    private  val BANNER_SEARCH_ID = "BANNER_SEARCH_ID"
    private  val BANNER_AD_INTERVAL = "BANNER_AD_INTERVAL"

    // Interstitial Ads Keys
    private  val IS_INTERSTITIAL_AD_ENABLED = "IS_INTERSTITIAL_AD_ENABLED"
    private  val INTERSTITIAL_AD_ID = "INTERSTITIAL_AD_ID"
    private  val IS_INTERSTITIAL_POST_DETAILS = "IS_INTERSTITIAL_POST_DETAILS"
    private  val INTERSTITIAL_POST_DETAIL_ID = "INTERSTITIAL_POST_DETAIL_ID"
    private  val INTERSTITIAL_AD_INTERVAL = "INTERSTITIAL_AD_INTERVAL"

    // Native Ads Keys
    private  val IS_NATIVE_AD_ENABLED = "IS_NATIVE_AD_ENABLED"
    private  val NATIVE_AD_ID = "NATIVE_AD_ID"
    private  val IS_NATIVE_AD_POST_DETAILS = "IS_NATIVE_AD_POST_DETAILS"
    private  val NATIVE_AD_POST_DETAIL_ID = "NATIVE_AD_POST_DETAIL_ID"
    private  val NATIVE_AD_STYLE = "NATIVE_AD_STYLE"

    // App Open Ads Keys
    private  val IS_APP_OPEN_AD_ENABLED = "IS_APP_OPEN_AD_ENABLED"
    private  val APP_OPEN_AD_ID = "APP_OPEN_AD_ID"

    // Rewarded Ads Keys
    private  val IS_REWARDED_AD_ENABLED = "IS_REWARDED_AD_ENABLED"
    private  val REWARDED_AD_ID = "REWARDED_AD_ID"

    // Families Policy Keys
    private  val IS_FAMILIES_APP = "IS_FAMILIES_APP"
    private  val MAX_ADS_PER_PAGE = "MAX_ADS_PER_PAGE"
    private  val AD_CONTENT_RATING = "AD_CONTENT_RATING"
    private  val CHILD_DIRECTED_TREATMENT = "CHILD_DIRECTED_TREATMENT"
    private  val IS_FAMILIES_CERTIFIED_SDK = "IS_FAMILIES_CERTIFIED_SDK"
    private  val FAMILIES_SDK_VERSION = "FAMILIES_SDK_VERSION"

    // GDPR & Privacy Keys
    private  val IS_GDPR_REQUIRED = "IS_GDPR_REQUIRED"
    private  val IS_CCPA_REQUIRED = "IS_CCPA_REQUIRED"
    private  val USER_CONSENT_STATUS = "USER_CONSENT_STATUS"
    private  val IS_PERSONALIZED_ADS = "IS_PERSONALIZED_ADS"

    // Ad Manager Keys
    private  val IS_AD_MANAGER_ENABLED = "IS_AD_MANAGER_ENABLED"
    private  val AD_MANAGER_PUBLISHER_ID = "AD_MANAGER_PUBLISHER_ID"

    // Test Mode Keys
    private  val IS_TEST_MODE = "IS_TEST_MODE"
    private  val TEST_DEVICE_IDS = "TEST_DEVICE_IDS"

    // Ad Counter Keys
    private  val AD_COUNTER = "AD_COUNTER"
    private  val LAST_AD_SHOWN_TIME = "LAST_AD_SHOWN_TIME"

    // Default Values
    private  val DEFAULT_AD_INTERVAL = 3
    private  val DEFAULT_MAX_ADS_PER_PAGE = 1 // สำหรับแอปครอบครัว
    private  val DEFAULT_AD_CONTENT_RATING = "G" // General audience

    private  val FAMILY_SAFE_ADS_ONLY = "family_safe_ads_only"


    init {
        this.context = context
        sharedPreferences = context.getSharedPreferences("ads_setting", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun saveAds(
        adStatus: String?,
        adType: String?,
        backupAds: String?,
        admobPublisherId: String?,
        admobAppId: String?,
        admobBannerUnitId: String?,
        admobInterstitialUnitId: String?,
        admobNativeUnitId: String?,
        admobAppOpenAdUnitId: String?,
        adManagerBannerUnitId: String?,
        adManagerInterstitialUnitId: String?,
        adManagerNativeUnitId: String?,
        adManagerAppOpenAdUnitId: String?,
        fanBannerUnitId: String?,
        fanInterstitialUnitId: String?,
        fanNativeUnitId: String?,
        startappAppId: String?,
        unityGameId: String?,
        unityBannerPlacementId: String?,
        unityInterstitialPlacementId: String?,
        applovinBannerAdUnitId: String?,
        applovinInterstitialAdUnitId: String?,
        applovinNativeAdManualUnitId: String?,
        applovinAppOpenAdUnitId: String?,
        applovinBannerZoneId: String?,
        applovinBannerMrecZoneId: String?,
        applovinInterstitialZoneId: String?,
        ironSourceAppKey: String?,
        ironSourceBannerId: String?,
        ironSourceInterstitialId: String?,
        wortiseAppId: String?,
        wortiseAppOpenId: String?,
        wortiseBannerId: String?,
        wortiseInterstitialId: String?,
        wortiseNativeId: String?,
        interstitialAdInterval: Int,
        nativeAdInterval: Int,
        nativeAdIndex: Int
    ) {
        editor.putString("ad_status", adStatus)
        editor.putString("ad_type", adType)
        editor.putString("backup_ads", backupAds)
        editor.putString("admob_publisher_id", admobPublisherId)
        editor.putString("admob_app_id", admobAppId)
        editor.putString("admob_banner_unit_id", admobBannerUnitId)
        editor.putString("admob_interstitial_unit_id", admobInterstitialUnitId)
        editor.putString("admob_native_unit_id", admobNativeUnitId)
        editor.putString("admob_app_open_ad_unit_id", admobAppOpenAdUnitId)
        editor.putString("ad_manager_banner_unit_id", adManagerBannerUnitId)
        editor.putString("ad_manager_interstitial_unit_id", adManagerInterstitialUnitId)
        editor.putString("ad_manager_native_unit_id", adManagerNativeUnitId)
        editor.putString("ad_manager_app_open_ad_unit_id", adManagerAppOpenAdUnitId)
        editor.putString("fan_banner_unit_id", fanBannerUnitId)
        editor.putString("fan_interstitial_unit_id", fanInterstitialUnitId)
        editor.putString("fan_native_unit_id", fanNativeUnitId)
        editor.putString("startapp_app_id", startappAppId)
        editor.putString("unity_game_id", unityGameId)
        editor.putString("unity_banner_placement_id", unityBannerPlacementId)
        editor.putString("unity_interstitial_placement_id", unityInterstitialPlacementId)
        editor.putString("applovin_banner_ad_unit_id", applovinBannerAdUnitId)
        editor.putString("applovin_interstitial_ad_unit_id", applovinInterstitialAdUnitId)
        editor.putString("applovin_native_ad_manual_unit_id", applovinNativeAdManualUnitId)
        editor.putString("applovin_app_open_ad_unit_id", applovinAppOpenAdUnitId)
        editor.putString("applovin_banner_zone_id", applovinBannerZoneId)
        editor.putString("applovin_banner_mrec_zone_id", applovinBannerMrecZoneId)
        editor.putString("applovin_interstitial_zone_id", applovinInterstitialZoneId)
        editor.putString("ironsource_app_key", ironSourceAppKey)
        editor.putString("ironsource_banner_id", ironSourceBannerId)
        editor.putString("ironsource_interstitial_id", ironSourceInterstitialId)
        editor.putString("wortise_app_id", wortiseAppId)
        editor.putString("wortise_app_open_id", wortiseAppOpenId)
        editor.putString("wortise_banner_id", wortiseBannerId)
        editor.putString("wortise_interstitial_id", wortiseInterstitialId)
        editor.putString("wortise_native_id", wortiseNativeId)
        editor.putInt("interstitial_ad_interval", interstitialAdInterval)
        editor.putInt("native_ad_interval", nativeAdInterval)
        editor.putInt("native_ad_index", nativeAdIndex)
        editor.apply()
    }

    val adStatus: String
        get() = sharedPreferences.getString("ad_status", "0")!!

    val mainAds: String
        get() = sharedPreferences.getString("ad_type", "0")!!

    val backupAds: String
        get() = sharedPreferences.getString("backup_ads", "none")!!

    val adMobPublisherId: String
        get() = sharedPreferences.getString("admob_publisher_id", "0")!!

    val adMobAppId: String
        get() = sharedPreferences.getString("admob_app_id", "0")!!

    val adMobBannerId: String
        get() = sharedPreferences.getString("admob_banner_unit_id", "0")!!

    val adMobInterstitialId: String
        get() = sharedPreferences.getString("admob_interstitial_unit_id", "0")!!

    val adMobNativeId: String
        get() = sharedPreferences.getString("admob_native_unit_id", "0")!!

    val adMobAppOpenAdId: String
        get() = sharedPreferences.getString("admob_app_open_ad_unit_id", "0")!!

    val adManagerBannerId: String
        get() = sharedPreferences.getString("ad_manager_banner_unit_id", "0")!!

    val adManagerInterstitialId: String
        get() = sharedPreferences.getString("ad_manager_interstitial_unit_id", "0")!!

    val adManagerNativeId: String
        get() = sharedPreferences.getString("ad_manager_native_unit_id", "0")!!

    val adManagerAppOpenAdId: String
        get() = sharedPreferences.getString("ad_manager_app_open_ad_unit_id", "0")!!

    val fanBannerId: String
        get() = sharedPreferences.getString("fan_banner_unit_id", "0")!!

    val fanInterstitialId: String
        get() = sharedPreferences.getString("fan_interstitial_unit_id", "0")!!

    val fanNativeId: String
        get() = sharedPreferences.getString("fan_native_unit_id", "0")!!

    val startappAppId: String
        get() = sharedPreferences.getString("startapp_app_id", "0")!!

    val unityGameId: String
        get() = sharedPreferences.getString("unity_game_id", "0")!!

    val unityBannerPlacementId: String
        get() = sharedPreferences.getString("unity_banner_placement_id", "banner")!!

    val unityInterstitialPlacementId: String
        get() = sharedPreferences.getString("unity_interstitial_placement_id", "video")!!

    val appLovinBannerAdUnitId: String
        get() = sharedPreferences.getString("applovin_banner_ad_unit_id", "0")!!

    val appLovinInterstitialAdUnitId: String
        get() = sharedPreferences.getString("applovin_interstitial_ad_unit_id", "0")!!

    val appLovinNativeAdManualUnitId: String
        get() = sharedPreferences.getString("applovin_native_ad_manual_unit_id", "0")!!

    val appLovinAppOpenAdUnitId: String
        get() = sharedPreferences.getString("applovin_app_open_ad_unit_id", "0")!!

    val appLovinBannerZoneId: String
        get() = sharedPreferences.getString("applovin_banner_zone_id", "0")!!

    val appLovinBannerMrecZoneId: String
        get() = sharedPreferences.getString("applovin_banner_mrec_zone_id", "0")!!

    val appLovinInterstitialZoneId: String
        get() = sharedPreferences.getString("applovin_interstitial_zone_id", "0")!!

    val ironSourceAppKey: String
        get() = sharedPreferences.getString("ironsource_app_key", "0")!!

    val ironSourceBannerId: String
        get() = sharedPreferences.getString("ironsource_banner_id", "0")!!

    val ironSourceInterstitialId: String
        get() = sharedPreferences.getString("ironsource_interstitial_id", "0")!!

    val wortiseAppId: String
        get() = sharedPreferences.getString("wortise_app_id", "0")!!

    val wortiseAppOpenId: String
        get() = sharedPreferences.getString("wortise_app_open_id", "0")!!

    val wortiseBannerId: String
        get() = sharedPreferences.getString("wortise_banner_id", "0")!!

    val wortiseInterstitialId: String
        get() = sharedPreferences.getString("wortise_interstitial_id", "0")!!

    val wortiseNativeId: String
        get() = sharedPreferences.getString("wortise_native_id", "0")!!



    val nativeAdInterval: Int
        get() = sharedPreferences.getInt("native_ad_interval", 0)

    val nativeAdIndex: Int
        get() = sharedPreferences.getInt("native_ad_index", 0)

    val dateTime: String
        get() = sharedPreferences.getString("date_time", "0")!!

    var allowFamilySafeAdsOnly: Boolean
        get() = sharedPreferences.getBoolean(FAMILY_SAFE_ADS_ONLY, true)
        set(value) = sharedPreferences.edit().putBoolean(FAMILY_SAFE_ADS_ONLY, value).apply()



    //placement
    fun setPlacement(
        isBannerHome: Boolean,
        isBannerPostDetails: Boolean,
        isBannerCategoryDetails: Boolean,
        isBannerSearch: Boolean,
        isInterstitialPostList: Boolean,
        isInterstitialPostDetails: Boolean,
        isNativeAdPostList: Boolean,
        isNativeAdPostDetails: Boolean,
        isNativeAdExitDialog: Boolean,
        isAppOpenAdOnStart: Boolean,
        isAppOpenAdOnResume: Boolean
    ) {
        editor.putBoolean("banner_home", isBannerHome)
        editor.putBoolean("banner_post_details", isBannerPostDetails)
        editor.putBoolean("banner_category_details", isBannerCategoryDetails)
        editor.putBoolean("banner_search", isBannerSearch)
        editor.putBoolean("interstitial_post_list", isInterstitialPostList)
        editor.putBoolean("interstitial_post_details", isInterstitialPostDetails)
        editor.putBoolean("native_ad_post_list", isNativeAdPostList)
        editor.putBoolean("native_ad_post_details", isNativeAdPostDetails)
        editor.putBoolean("native_ad_exit_dialog", isNativeAdExitDialog)
        editor.putBoolean("app_open_ad_on_start", isAppOpenAdOnStart)
        editor.putBoolean("app_open_ad_on_resume", isAppOpenAdOnResume)
        editor.apply()
    }



    val isBannerCategoryDetails: Boolean
        get() = sharedPreferences.getBoolean("banner_category_details", true)


    val isInterstitialPostList: Boolean
        get() = sharedPreferences.getBoolean("interstitial_post_list", true)

       var isNativeAdPostList: Boolean = false
           get() = sharedPreferences.getBoolean("native_ad_post_list", true)


    val isNativeAdExitDialog: Boolean
        get() = sharedPreferences.getBoolean("native_ad_exit_dialog", true)

    val isAppOpenAdOnStart: Boolean
        get() = sharedPreferences.getBoolean("app_open_ad_on_start", true)

    val isAppOpenAdOnResume: Boolean
        get() = sharedPreferences.getBoolean("app_open_ad_on_resume", true)

    var isOpenAd: Boolean
        get() = sharedPreferences.getBoolean("open_ads", false)
        set(isOpenAd) {
            editor.putBoolean("open_ads", isOpenAd)
            editor.apply()
        }

    fun setNativeAdStyle(
        nativeAdStylePostList: String?,
        nativeAdStyleVideoList: String?,
        nativeAdStylePostDetails: String?,
        nativeAdStyleExitDialog: String?
    ) {
        editor.putString("native_ad_style_post_list", nativeAdStylePostList)
        editor.putString("native_ad_style_video_list", nativeAdStyleVideoList)
        editor.putString("native_ad_style_post_details", nativeAdStylePostDetails)
        editor.putString("native_ad_style_exit_dialog", nativeAdStyleExitDialog)
        editor.apply()
    }

    val nativeAdStylePostList: String
        get() = sharedPreferences.getString("native_ad_style_post_list", "medium")!!


    val nativeAdStylePostDetails: String
        get() = sharedPreferences.getString("native_ad_style_post_details", "large")!!

    // ==================== General Ad Settings ====================

    var isAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_AD_ENABLED, true)
        set(isAdEnabled) {
            editor.putBoolean(IS_AD_ENABLED, isAdEnabled)
            editor.apply()
        }

    var isAdNetworkEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_AD_NETWORK_ENABLED, true)
        set(isAdNetworkEnabled) {
            editor.putBoolean(IS_AD_NETWORK_ENABLED, isAdNetworkEnabled)
            editor.apply()
        }

    var adNetworkType: String
        get() = sharedPreferences.getString(AD_NETWORK_TYPE, "admob") ?: "admob"
        set(adNetworkType) {
            editor.putString(AD_NETWORK_TYPE, adNetworkType)
            editor.apply()
        }

    // ==================== Banner Ad Settings ====================

    var isBannerAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_BANNER_AD_ENABLED, true)
        set(isBannerAdEnabled) {
            editor.putBoolean(IS_BANNER_AD_ENABLED, isBannerAdEnabled)
            editor.apply()
        }

    var bannerAdId: String
        get() = sharedPreferences.getString(BANNER_AD_ID, "ca-app-pub-3940256099942544/6300978111") ?: "ca-app-pub-3940256099942544/6300978111"
        set(bannerAdId) {
            editor.putString(BANNER_AD_ID, bannerAdId)
            editor.apply()
        }

    var isBannerHome: Boolean
        get() = sharedPreferences.getBoolean(IS_BANNER_HOME, true)
        set(isBannerHome) {
            editor.putBoolean(IS_BANNER_HOME, isBannerHome)
            editor.apply()
        }

    var bannerHomeId: String
        get() = sharedPreferences.getString(BANNER_HOME_ID, bannerAdId) ?: bannerAdId
        set(bannerHomeId) {
            editor.putString(BANNER_HOME_ID, bannerHomeId)
            editor.apply()
        }

    var isBannerPostDetails: Boolean
        get() = sharedPreferences.getBoolean(IS_BANNER_POST_DETAILS, true)
        set(isBannerPostDetails) {
            editor.putBoolean(IS_BANNER_POST_DETAILS, isBannerPostDetails)
            editor.apply()
        }

    var bannerPostDetailId: String
        get() = sharedPreferences.getString(BANNER_POST_DETAIL_ID, bannerAdId) ?: bannerAdId
        set(bannerPostDetailId) {
            editor.putString(BANNER_POST_DETAIL_ID, bannerPostDetailId)
            editor.apply()
        }

    var isBannerCategory: Boolean
        get() = sharedPreferences.getBoolean(IS_BANNER_CATEGORY, true)
        set(isBannerCategory) {
            editor.putBoolean(IS_BANNER_CATEGORY, isBannerCategory)
            editor.apply()
        }

    var bannerCategoryId: String
        get() = sharedPreferences.getString(BANNER_CATEGORY_ID, bannerAdId) ?: bannerAdId
        set(bannerCategoryId) {
            editor.putString(BANNER_CATEGORY_ID, bannerCategoryId)
            editor.apply()
        }

    var isBannerSearch: Boolean
        get() = sharedPreferences.getBoolean(IS_BANNER_SEARCH, true)
        set(isBannerSearch) {
            editor.putBoolean(IS_BANNER_SEARCH, isBannerSearch)
            editor.apply()
        }

    var bannerSearchId: String
        get() = sharedPreferences.getString(BANNER_SEARCH_ID, bannerAdId) ?: bannerAdId
        set(bannerSearchId) {
            editor.putString(BANNER_SEARCH_ID, bannerSearchId)
            editor.apply()
        }

    var bannerAdInterval: Int
        get() = sharedPreferences.getInt(BANNER_AD_INTERVAL, DEFAULT_AD_INTERVAL)
        set(bannerAdInterval) {
            editor.putInt(BANNER_AD_INTERVAL, bannerAdInterval)
            editor.apply()
        }

    // ==================== Interstitial Ad Settings ====================

    var isInterstitialAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_INTERSTITIAL_AD_ENABLED, true)
        set(isInterstitialAdEnabled) {
            editor.putBoolean(IS_INTERSTITIAL_AD_ENABLED, isInterstitialAdEnabled)
            editor.apply()
        }

    var interstitialAdId: String
        get() = sharedPreferences.getString(INTERSTITIAL_AD_ID, "ca-app-pub-3940256099942544/1033173712") ?: "ca-app-pub-3940256099942544/1033173712"
        set(interstitialAdId) {
            editor.putString(INTERSTITIAL_AD_ID, interstitialAdId)
            editor.apply()
        }

    var isInterstitialPostDetails: Boolean
        get() = sharedPreferences.getBoolean(IS_INTERSTITIAL_POST_DETAILS, true)
        set(isInterstitialPostDetails) {
            editor.putBoolean(IS_INTERSTITIAL_POST_DETAILS, isInterstitialPostDetails)
            editor.apply()
        }

    var interstitialPostDetailId: String
        get() = sharedPreferences.getString(INTERSTITIAL_POST_DETAIL_ID, interstitialAdId) ?: interstitialAdId
        set(interstitialPostDetailId) {
            editor.putString(INTERSTITIAL_POST_DETAIL_ID, interstitialPostDetailId)
            editor.apply()
        }

    var interstitialAdInterval: Int
        get() = sharedPreferences.getInt(INTERSTITIAL_AD_INTERVAL, DEFAULT_AD_INTERVAL)
        set(interstitialAdInterval) {
            editor.putInt(INTERSTITIAL_AD_INTERVAL, interstitialAdInterval)
            editor.apply()
        }

    // ==================== Native Ad Settings ====================

    var isNativeAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_NATIVE_AD_ENABLED, true)
        set(isNativeAdEnabled) {
            editor.putBoolean(IS_NATIVE_AD_ENABLED, isNativeAdEnabled)
            editor.apply()
        }

    var nativeAdId: String
        get() = sharedPreferences.getString(NATIVE_AD_ID, "ca-app-pub-3940256099942544/2247696110") ?: "ca-app-pub-3940256099942544/2247696110"
        set(nativeAdId) {
            editor.putString(NATIVE_AD_ID, nativeAdId)
            editor.apply()
        }

    var isNativeAdPostDetails: Boolean
        get() = sharedPreferences.getBoolean(IS_NATIVE_AD_POST_DETAILS, false) // ปิดไว้สำหรับแอปครอบครัว
        set(isNativeAdPostDetails) {
            editor.putBoolean(IS_NATIVE_AD_POST_DETAILS, isNativeAdPostDetails)
            editor.apply()
        }

    var nativeAdPostDetailId: String
        get() = sharedPreferences.getString(NATIVE_AD_POST_DETAIL_ID, nativeAdId) ?: nativeAdId
        set(nativeAdPostDetailId) {
            editor.putString(NATIVE_AD_POST_DETAIL_ID, nativeAdPostDetailId)
            editor.apply()
        }

    var nativeAdStyle: String
        get() = sharedPreferences.getString(NATIVE_AD_STYLE, "medium") ?: "medium"
        set(nativeAdStyle) {
            editor.putString(NATIVE_AD_STYLE, nativeAdStyle)
            editor.apply()
        }

    // ==================== App Open Ad Settings ====================

    var isAppOpenAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_APP_OPEN_AD_ENABLED, false) // ปิดไว้สำหรับแอปครอบครัว
        set(isAppOpenAdEnabled) {
            editor.putBoolean(IS_APP_OPEN_AD_ENABLED, isAppOpenAdEnabled)
            editor.apply()
        }

    var appOpenAdId: String
        get() = sharedPreferences.getString(APP_OPEN_AD_ID, "ca-app-pub-3940256099942544/3419835294") ?: "ca-app-pub-3940256099942544/3419835294"
        set(appOpenAdId) {
            editor.putString(APP_OPEN_AD_ID, appOpenAdId)
            editor.apply()
        }

    // ==================== Rewarded Ad Settings ====================

    var isRewardedAdEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_REWARDED_AD_ENABLED, false) // ปิดไว้สำหรับแอปครอบครัว
        set(isRewardedAdEnabled) {
            editor.putBoolean(IS_REWARDED_AD_ENABLED, isRewardedAdEnabled)
            editor.apply()
        }

    var rewardedAdId: String
        get() = sharedPreferences.getString(REWARDED_AD_ID, "ca-app-pub-3940256099942544/5224354917") ?: "ca-app-pub-3940256099942544/5224354917"
        set(rewardedAdId) {
            editor.putString(REWARDED_AD_ID, rewardedAdId)
            editor.apply()
        }

    // ==================== Families Policy Settings ====================

    var isFamiliesApp: Boolean
        get() = sharedPreferences.getBoolean(IS_FAMILIES_APP, true) // ตั้งค่าเป็น true สำหรับแอปครอบครัว
        set(isFamiliesApp) {
            editor.putBoolean(IS_FAMILIES_APP, isFamiliesApp)
            editor.apply()
        }

    var maxAdsPerPage: Int
        get() = sharedPreferences.getInt(MAX_ADS_PER_PAGE, DEFAULT_MAX_ADS_PER_PAGE)
        set(maxAdsPerPage) {
            editor.putInt(MAX_ADS_PER_PAGE, maxAdsPerPage)
            editor.apply()
        }

    var adContentRating: String
        get() = sharedPreferences.getString(AD_CONTENT_RATING, DEFAULT_AD_CONTENT_RATING) ?: DEFAULT_AD_CONTENT_RATING
        set(adContentRating) {
            editor.putString(AD_CONTENT_RATING, adContentRating)
            editor.apply()
        }

    var isChildDirectedTreatment: Boolean
        get() = sharedPreferences.getBoolean(CHILD_DIRECTED_TREATMENT, true)
        set(isChildDirectedTreatment) {
            editor.putBoolean(CHILD_DIRECTED_TREATMENT, isChildDirectedTreatment)
            editor.apply()
        }

    var isFamiliesCertifiedSdk: Boolean
        get() = sharedPreferences.getBoolean(IS_FAMILIES_CERTIFIED_SDK, true)
        set(isFamiliesCertifiedSdk) {
            editor.putBoolean(IS_FAMILIES_CERTIFIED_SDK, isFamiliesCertifiedSdk)
            editor.apply()
        }

    var familiesSdkVersion: String
        get() = sharedPreferences.getString(FAMILIES_SDK_VERSION, "22.6.0") ?: "22.6.0"
        set(familiesSdkVersion) {
            editor.putString(FAMILIES_SDK_VERSION, familiesSdkVersion)
            editor.apply()
        }

    // ==================== GDPR & Privacy Settings ====================

    var isGDPRRequired: Boolean
        get() = sharedPreferences.getBoolean(IS_GDPR_REQUIRED, false)
        set(isGDPRRequired) {
            editor.putBoolean(IS_GDPR_REQUIRED, isGDPRRequired)
            editor.apply()
        }

    var isCCPARequired: Boolean
        get() = sharedPreferences.getBoolean(IS_CCPA_REQUIRED, false)
        set(isCCPARequired) {
            editor.putBoolean(IS_CCPA_REQUIRED, isCCPARequired)
            editor.apply()
        }

    var userConsentStatus: String
        get() = sharedPreferences.getString(USER_CONSENT_STATUS, "unknown") ?: "unknown"
        set(userConsentStatus) {
            editor.putString(USER_CONSENT_STATUS, userConsentStatus)
            editor.apply()
        }

    var isPersonalizedAds: Boolean
        get() = sharedPreferences.getBoolean(IS_PERSONALIZED_ADS, false) // สำหรับแอปครอบครัวควรเป็น false
        set(isPersonalizedAds) {
            editor.putBoolean(IS_PERSONALIZED_ADS, isPersonalizedAds)
            editor.apply()
        }

    // ==================== Ad Manager Settings ====================

    var isAdManagerEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_AD_MANAGER_ENABLED, false)
        set(isAdManagerEnabled) {
            editor.putBoolean(IS_AD_MANAGER_ENABLED, isAdManagerEnabled)
            editor.apply()
        }

    var adManagerPublisherId: String
        get() = sharedPreferences.getString(AD_MANAGER_PUBLISHER_ID, "") ?: ""
        set(adManagerPublisherId) {
            editor.putString(AD_MANAGER_PUBLISHER_ID, adManagerPublisherId)
            editor.apply()
        }

    // ==================== Test Mode Settings ====================

    var isTestMode: Boolean
        get() = sharedPreferences.getBoolean(IS_TEST_MODE, true) // เปิด Test Mode เป็น default
        set(isTestMode) {
            editor.putBoolean(IS_TEST_MODE, isTestMode)
            editor.apply()
        }

    var testDeviceIds: String
        get() = sharedPreferences.getString(TEST_DEVICE_IDS, "") ?: ""
        set(testDeviceIds) {
            editor.putString(TEST_DEVICE_IDS, testDeviceIds)
            editor.apply()
        }

    // ==================== Ad Counter & Timing ====================

    var counter: Int
        get() = sharedPreferences.getInt(AD_COUNTER, 0)
        set(counter) {
            editor.putInt(AD_COUNTER, counter)
            editor.apply()
        }

    var lastAdShownTime: Long
        get() = sharedPreferences.getLong(LAST_AD_SHOWN_TIME, 0)
        set(lastAdShownTime) {
            editor.putLong(LAST_AD_SHOWN_TIME, lastAdShownTime)
            editor.apply()
        }

    // ==================== Helper Methods ====================

    /**
     * บันทึกค่า counter
     */
    fun saveCounter(value: Int) {
        counter = value
    }

    /**
     * เพิ่มค่า counter
     */
    fun incrementCounter() {
        counter = counter + 1
    }

    /**
     * รีเซ็ต counter
     */
    fun resetCounter() {
        counter = 0
    }

    /**
     * ตรวจสอบว่าควรแสดงโฆษณาหรือไม่ตาม interval
     */
    fun shouldShowAd(adInterval: Int = interstitialAdInterval): Boolean {
        return counter >= adInterval
    }

    /**
     * บันทึกเวลาที่แสดงโฆษณาครั้งล่าสุด
     */
    fun recordAdShown() {
        lastAdShownTime = System.currentTimeMillis()
        resetCounter()
    }

    /**
     * ตรวจสอบว่าเวลาผ่านไปพอที่จะแสดงโฆษณาใหม่หรือไม่
     */
    fun canShowAdAgain(minIntervalMillis: Long = 30000): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastAdShownTime) >= minIntervalMillis
    }

    /**
     * รับ Ad Unit ID สำหรับหน้าเฉพาะ
     */
    fun getAdUnitIdForPage(pageType: String, adType: String): String {
        return when (pageType) {
            "home" -> {
                when (adType) {
                    "banner" -> bannerHomeId
                    "interstitial" -> interstitialAdId
                    "native" -> nativeAdId
                    else -> bannerHomeId
                }
            }
            "post_detail" -> {
                when (adType) {
                    "banner" -> bannerPostDetailId
                    "interstitial" -> interstitialPostDetailId
                    "native" -> nativeAdPostDetailId
                    else -> bannerPostDetailId
                }
            }
            "category" -> {
                when (adType) {
                    "banner" -> bannerCategoryId
                    else -> bannerCategoryId
                }
            }
            "search" -> {
                when (adType) {
                    "banner" -> bannerSearchId
                    else -> bannerSearchId
                }
            }
            else -> {
                when (adType) {
                    "banner" -> bannerAdId
                    "interstitial" -> interstitialAdId
                    "native" -> nativeAdId
                    "app_open" -> appOpenAdId
                    "rewarded" -> rewardedAdId
                    else -> bannerAdId
                }
            }
        }
    }

    /**
     * ตรวจสอบว่าหน้านี้สามารถแสดงโฆษณาชนิดนี้ได้หรือไม่
     */
    fun isAdEnabledForPage(pageType: String, adType: String): Boolean {
        if (!isAdEnabled || !isAdNetworkEnabled) return false

        return when (pageType) {
            "home" -> {
                when (adType) {
                    "banner" -> isBannerHome && isBannerAdEnabled
                    "interstitial" -> isInterstitialAdEnabled
                    "native" -> isNativeAdEnabled
                    else -> false
                }
            }
            "post_detail" -> {
                when (adType) {
                    "banner" -> isBannerPostDetails && isBannerAdEnabled
                    "interstitial" -> isInterstitialPostDetails && isInterstitialAdEnabled
                    "native" -> isNativeAdPostDetails && isNativeAdEnabled
                    else -> false
                }
            }
            "category" -> {
                when (adType) {
                    "banner" -> isBannerCategory && isBannerAdEnabled
                    else -> false
                }
            }
            "search" -> {
                when (adType) {
                    "banner" -> isBannerSearch && isBannerAdEnabled
                    else -> false
                }
            }
            else -> {
                when (adType) {
                    "banner" -> isBannerAdEnabled
                    "interstitial" -> isInterstitialAdEnabled
                    "native" -> isNativeAdEnabled
                    "app_open" -> isAppOpenAdEnabled
                    "rewarded" -> isRewardedAdEnabled
                    else -> false
                }
            }
        }
    }

    /**
     * ตั้งค่าเริ่มต้นสำหรับแอปครอบครัว
     */
    fun setFamiliesDefaults() {
        // ปิดการตั้งค่าที่ไม่เหมาะสำหรับแอปครอบครัว
        isNativeAdPostDetails = false
        isAppOpenAdEnabled = false
        isRewardedAdEnabled = false

        // ตั้งค่าสำหรับ Families Policy
        maxAdsPerPage = 1
        adContentRating = "G"
        isChildDirectedTreatment = true
        isFamiliesCertifiedSdk = true
        isPersonalizedAds = false

        // ตั้งค่า test mode เป็น false สำหรับ production
        isTestMode = false

        editor.apply()
    }

    /**
     * ตั้งค่าโหมดทดสอบ
     */
    fun setTestModeDefaults() {
        // ใช้ Test Ad Unit IDs
        bannerAdId = "ca-app-pub-3940256099942544/6300978111"
        bannerHomeId = "ca-app-pub-3940256099942544/6300978111"
        bannerPostDetailId = "ca-app-pub-3940256099942544/6300978111"
        bannerCategoryId = "ca-app-pub-3940256099942544/6300978111"
        bannerSearchId = "ca-app-pub-3940256099942544/6300978111"

        interstitialAdId = "ca-app-pub-3940256099942544/1033173712"
        interstitialPostDetailId = "ca-app-pub-3940256099942544/1033173712"

        nativeAdId = "ca-app-pub-3940256099942544/2247696110"
        nativeAdPostDetailId = "ca-app-pub-3940256099942544/2247696110"

        appOpenAdId = "ca-app-pub-3940256099942544/3419835294"
        rewardedAdId = "ca-app-pub-3940256099942544/5224354917"

        // เปิด Test Mode
        isTestMode = true

        editor.apply()
    }

    /**
     * ล้างการตั้งค่าทั้งหมด (สำหรับการทดสอบ)
     */
    fun clearAllSettings() {
        editor.clear()
        editor.apply()
    }

    /**
     * รับการตั้งค่าทั้งหมดเป็น Map
     */
    fun getAllSettings(): Map<String, Any> {
        return mapOf(
            // General
            "isAdEnabled" to isAdEnabled,
            "isAdNetworkEnabled" to isAdNetworkEnabled,
            "adNetworkType" to adNetworkType,

            // Banner
            "isBannerAdEnabled" to isBannerAdEnabled,
            "isBannerHome" to isBannerHome,
            "isBannerPostDetails" to isBannerPostDetails,
            "isBannerCategory" to isBannerCategory,
            "isBannerSearch" to isBannerSearch,
            "bannerAdInterval" to bannerAdInterval,

            // Interstitial
            "isInterstitialAdEnabled" to isInterstitialAdEnabled,
            "isInterstitialPostDetails" to isInterstitialPostDetails,
            "interstitialAdInterval" to interstitialAdInterval,

            // Native
            "isNativeAdEnabled" to isNativeAdEnabled,
            "isNativeAdPostDetails" to isNativeAdPostDetails,

            // Families
            "isFamiliesApp" to isFamiliesApp,
            "maxAdsPerPage" to maxAdsPerPage,
            "adContentRating" to adContentRating,
            "isChildDirectedTreatment" to isChildDirectedTreatment,
            "isFamiliesCertifiedSdk" to isFamiliesCertifiedSdk,

            // GDPR
            "isGDPRRequired" to isGDPRRequired,
            "isPersonalizedAds" to isPersonalizedAds,

            // Test Mode
            "isTestMode" to isTestMode,

            // Counter
            "counter" to counter,
            "lastAdShownTime" to lastAdShownTime
        )
    }

    /**
     * พิมพ์การตั้งค่าทั้งหมด (สำหรับ debugging)
     */
    fun printAllSettings() {
        getAllSettings().forEach { (key, value) ->
            println("$key = $value")
        }
    }
}


