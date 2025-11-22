package com.pongploydev.videowatchfeature.config;


import com.pongploydev.videowatchfeature.Constant;

public class AppConfig {

    //your Server Key obtained from the admin panel
    public static final String SERVER_KEY = "WVVoU01HTklUVFpNZVRsM1lqSTFibU5IZUhabFYxSnNaR2sxYW1JeU1IWmpTRlpwWWtkc2Frd3piSFprV0VwbVpHMXNhMXBYT1hwWU1rNXZXVmMxZFZwWGVHWlpXRUozWWtkc2FsbFlVbkJpTWpWS1drWTVhbUl5TUhWalJ6bDFXak5DYzJJemJHdGFXRmwxV2xkU01Wa3lSakJoVnpsMVRHMHhiRnBIYkdoWldFSjM=";

    //your Rest API Key obtained from the admin panel
    public static final String REST_API_KEY = "cda11kfV2SDYTQrvIcAleR75FBh0qN4JjLiZym8sWtOx3oKgbM";

    //Constant.VIDEO_LIST_DEFAULT or Constant.VIDEO_LIST_COMPACT
    public static final int DEFAULT_VIDEO_VIEW_TYPE = Constant.VIDEO_LIST_DEFAULT;

    //Enable it with true value if want to the app will force to display open ads first before start the main menu
    //Longer duration to start the app may occur depending on internet connection or open ad response time itself
    public static final boolean FORCE_TO_SHOW_APP_OPEN_AD_ON_START = false;

    //press back twice to exit from player screen
    public static final boolean PRESS_BACK_TWICE_TO_CLOSE_PLAYER = false;

    //layout customization
    public static final boolean SET_CATEGORY_AS_MAIN_MENU = false;
    public static final boolean FORCE_PLAYER_TO_LANDSCAPE = false;
    public static final boolean ENABLE_VIEW_COUNT = true;
    public static final boolean ENABLE_DATE_DISPLAY = true;
    public static final boolean DISPLAY_DATE_AS_TIME_AGO = true;
    public static final boolean ENABLE_VIDEO_COUNT_ON_CATEGORY = true;

    //highly recommended to keep with false value to prevent the app accessed using VPN for security reason
    public static final boolean ALLOW_APP_ACCESSED_USING_VPN = false;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

    //display dialog for exit confirmation
    public static final boolean ENABLE_EXIT_DIALOG = true;

    public static final boolean ENABLE_GDPR_UMP_SDK = true;

    //load more for next list videos
    public static final int LOAD_MORE = 10;

    //delay splash screen
    public static final int DELAY_SPLASH = 100;



    public static final boolean ENABLE_NEW_APP_DESIGN = true;

    //show one latest news as header view
    public static final boolean SHOW_HEADER_VIEW = true;

    //show news list in large view
    public static final boolean BIG_NEWS_LIST_STYLE = false;

    //show short description in the news list
    public static final boolean SHOW_EXCERPT_IN_POST_LIST = true;

    //show total news in each category
    public static final boolean SHOW_POST_COUNT_IN_CATEGORY = false;

    //video player orientation
    public static final boolean FORCE_VIDEO_PLAYER_TO_LANDSCAPE = false;

    //date display configuration
    public static final boolean SHOW_POST_DATE = true;
    public static final boolean SHOW_DATE_DISPLAY_AS_TIME_AGO = false;

    //enable view count in the news description
    public static final boolean SHOW_POST_VIEW_COUNT = true;


    //set false to disable copy text in the news description
    public static final boolean ENABLE_TEXT_SELECTION = false;

    //open link in the news description using external web browser
    public static final boolean OPEN_LINK_INSIDE_APP = true;


    public static final boolean SET_CATEGORY_AS_MAIN_PAGE = false;


    //Delay splash screen
    public static final int DELAY_SPLASH_SCREEN = 1000;
}