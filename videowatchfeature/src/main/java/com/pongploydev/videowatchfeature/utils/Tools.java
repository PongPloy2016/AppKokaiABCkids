package com.pongploydev.videowatchfeature.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.pongploydev.videowatchfeature.MainActivity;
import com.pongploydev.videowatchfeature.R;
import com.pongploydev.videowatchfeature.config.AppConfig;
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref;
import com.pongploydev.videowatchfeature.model.Video;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Tools {

    Activity activity;
    private BottomSheetDialog mBottomSheetDialog;
//    DbFavorite dbFavorite;
//    SharedPref sharedPref;
//    DbHandler dbHandler;


    public Tools(Activity activity) {
        this.activity = activity;
//        this.sharedPref = new SharedPref(activity);
//        this.dbFavorite = new DbFavorite(activity);
//        this.dbHandler =  new DbHandler(activity);
    }

    public static void getTheme(Context context) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getIsDarkTheme()) {
            //context.setTheme(R.style.AppDarkTheme);
        } else {
            context.setTheme(R.style.AppTheme);
        }
        EdgeToEdge.enable((ComponentActivity) context);
    }

//    public static void setNavigation(Activity activity) {
//        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.root_view), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//        SharedPref sharedPref = new SharedPref(activity);
//        if (sharedPref.getIsDarkTheme()) {
//            WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
//            Tools.darkNavigation(activity);
//        } else {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
//            }
//            Tools.lightNavigation(activity);
//        }
//        setLayoutDirection(activity);
//    }



    public static void setLayoutDirection(Activity activity) {
        if (AppConfig.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           // activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dark_bottom_navigation));
            //activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_dark_status_bar));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_light_bottom_navigation));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_light_status_bar));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar, String title, boolean backButton) {
        SharedPref sharedPref = new SharedPref(activity);
        activity.setSupportActionBar(toolbar);
        if (sharedPref.getIsDarkTheme()) {
           // toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_primary));
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_light_primary));
        }
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(backButton);
            activity.getSupportActionBar().setHomeButtonEnabled(backButton);
            activity.getSupportActionBar().setTitle(title);
        }
    }

//    public static void notificationOpenHandler(Context context, Intent getIntent) {
//        String id = getIntent.getStringExtra(OneSignalPush.EXTRA_ID);
//        String title = getIntent.getStringExtra(OneSignalPush.EXTRA_TITLE);
//        String message = getIntent.getStringExtra(OneSignalPush.EXTRA_MESSAGE);
//        String bigImage = getIntent.getStringExtra(OneSignalPush.EXTRA_IMAGE);
//        String launchUrl = getIntent.getStringExtra(OneSignalPush.EXTRA_LAUNCH_URL);
//        String link = getIntent.getStringExtra(OneSignalPush.EXTRA_LINK);
//        String uniqueId = getIntent.getStringExtra(OneSignalPush.EXTRA_UNIQUE_ID);
//
//        SharedPref sharedPref = new SharedPref(context);
//        String postId;
//        if (sharedPref.getPushNotificationProvider().equals("onesignal")) {
//            postId = OneSignalPush.AdditionalData.postId;
//        } else {
//            postId = getIntent.getStringExtra(OneSignalPush.EXTRA_POST_ID);
//        }
//
//        long _postId;
//        if (postId != null && !postId.equals("")) {
//            _postId = Long.parseLong(postId);
//        } else {
//            _postId = 0;
//        }
//
//        if (getIntent.hasExtra(OneSignalPush.EXTRA_UNIQUE_ID)) {
//            if (_postId > 0) {
//                Intent intent = new Intent(context, MainActivity.class);
//                intent.putExtra("id", postId);
//                context.startActivity(intent);
//                OneSignalPush.AdditionalData.postId = "0";
//            }
//
//            if (launchUrl != null && !launchUrl.equals("")) {
//                Tools.openActivityLaunchUrl(context, title, launchUrl);
//            } else {
//                if (link != null && !link.equals("")) {
//                    if (!link.equals("0")) {
//                        Tools.openActivityLaunchUrl(context, title, link);
//                    }
//                }
//            }
//
//        }
//    }

    public static void openActivityLaunchUrl(Context context, String title, String url) {
        if (url.contains("play.google.com") || url.contains("?target=external")) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
//            Intent intent = new Intent(context, ActivityWebView.class);
//            intent.putExtra("title", title);
//            intent.putExtra("url", url);
//            context.startActivity(intent);
        }
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static String decode(String code) {
        return decodeBase64(decodeBase64(decodeBase64(code)));
    }

    public static String decodeBase64(String code) {
        byte[] valueDecoded = Base64.decode(code.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return new String(valueDecoded);
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return milis;
    }

    @SuppressWarnings("deprecation")
    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivity.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.d("Network", "NETWORKNAME: " + anInfo.getTypeName());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String getJSONString(String url) {
        String jsonString = null;
        HttpURLConnection linkConnection = null;
        try {
            URL linkurl = new URL(url);
            linkConnection = (HttpURLConnection) linkurl.openConnection();
            int responseCode = linkConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream linkinStream = linkConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int j = 0;
                while ((j = linkinStream.read()) != -1) {
                    baos.write(j);
                }
                byte[] data = baos.toByteArray();
                jsonString = new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (linkConnection != null) {
                linkConnection.disconnect();
            }
        }
        return jsonString;
    }

    public static String getFormatedDateSimple(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static CharSequence getTimeAgo(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            try {
                long time = sdf.parse(date_str).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void getVideoPosition(Activity activity, Intent intent) {
        if (intent.hasExtra("video_position")) {
            String select = intent.getStringExtra("video_position");
            if (select != null) {
                if (select.equals("video_position")) {
                    if (activity instanceof MainActivity) {
                        // ((MainActivity) activity).selectVideo();
                    }
                }
            }
        }
    }

    public static void getCategoryPosition(Activity activity, Intent intent) {
        if (intent.hasExtra("category_position")) {
            String select = intent.getStringExtra("category_position");
            if (select != null) {
                if (select.equals("category_position")) {
                    if (activity instanceof MainActivity) {
                        // ((MainActivity) activity).selectCategory();
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void displayPostDescription(Activity activity, WebView webView, String htmlData) {
        SharedPref sharedPref = new SharedPref(activity);
        if (webView == null) return; // Prevent crash

        // ⚙️ WebView settings
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setFocusableInTouchMode(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setFocusable(false);
        webView.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webView.getSettings();
        Resources res = activity.getResources();
//        int fontSize = res.getInteger(R.integer.font_size);
//        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";

        // 🎨 Theme styles
        String bg_paragraph;
        if (sharedPref.getIsDarkTheme()) {
            bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        // 🔤 Custom font and content styling
        String font_style_default = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {font-family: MyFont; font-size: medium; overflow-wrap: break-word; word-wrap: break-word; -ms-word-break: break-all; word-break: break-all; word-break: break-word; -ms-hyphens: auto; -moz-hyphens: auto; -webkit-hyphens: auto; hyphens: auto;}</style>";

        String htmlTemplate = "<html><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style>"
                + bg_paragraph
                + "</style></head><body>"
                + htmlData
                + "</body></html>";

        String htmlTemplateRTL = "<html dir='rtl'><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style>"
                + bg_paragraph
                + "</style></head><body>"
                + htmlData
                + "</body></html>";

        // 🔁 Load content
        if (AppConfig.ENABLE_RTL_MODE) {
            webView.loadDataWithBaseURL(null, htmlTemplateRTL, mimeType, encoding, null);
        } else {
            webView.loadDataWithBaseURL(null, htmlTemplate, mimeType, encoding, null);
        }

        // ✅ สำคัญ: ป้องกัน ScrollView ขโมยการเลื่อน
        // ตรวจสอบว่าเป็น PDF หรือไม่
        boolean isPDF = htmlData.toLowerCase().contains(".pdf")
                || htmlData.toLowerCase().contains("application/pdf")
                || htmlData.toLowerCase().contains("iframe")
                || htmlData.toLowerCase().contains("embed");

// ถ้าไม่ใช่ PDF ให้เปิด scroll (ไม่ให้พาเรนต์ intercept)

        if (isPDF) {
            // ❌ ปิด scroll ของ WebView สำหรับ PDF
            webView.setVerticalScrollBarEnabled(false);
            webView.setHorizontalScrollBarEnabled(false);
            webView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_MOVE ||
                        event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            });
        } else {
            // ✅ เปิด scroll และห้าม ScrollView intercept
            webView.setVerticalScrollBarEnabled(true);
            webView.setHorizontalScrollBarEnabled(false);
            webView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_MOVE ||
                        event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            });
        }


    }


//    public static void shareContentDetail(Activity activity, Post post) {
//
//        StringBuilder message = new StringBuilder();
//
//        String title = post.news_title ;
//        String description = post.news_description;
//        String url = post.video_url;
//
//        if (title != null && !title.isEmpty()) {
//            message.append(title).append("\n\n");
//        }
//
////        if (description != null && !description.isEmpty()) {
////            message.append(description).append("\n\n");
////        }
//
//        if (url != null && !url.isEmpty()) {
//            message.append(url);
//        }
//
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
//        sendIntent.setType("text/plain");
//
//        // Ensure the chooser is shown
//        Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
//        activity.startActivity(shareIntent);
//
//    }

    public static void shareContentDetail(Activity activity, Video video) {

        StringBuilder message = new StringBuilder();

        String title = video.video_title ;
        String description = video.video_description;
        String url = video.video_url;

        if (title != null && !title.isEmpty()) {
            message.append(title).append("\n\n");
        }

//        if (description != null && !description.isEmpty()) {
//            message.append(description).append("\n\n");
//        }

        if (url != null && !url.isEmpty()) {
            message.append(url);
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        sendIntent.setType("text/plain");

        // Ensure the chooser is shown
        Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
        activity.startActivity(shareIntent);

    }

  //  public void showBottomSheetDialog(Activity activity, View parentView, Video video) {
     //   @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.include_bottom_sheet, null);

//        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
//
//        TextView txtFavorite = view.findViewById(R.id.txt_favorite);
//
//        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
//        ImageView imgShare = view.findViewById(R.id.img_share);
//        ImageView imgReport = view.findViewById(R.id.img_report);

//        if (sharedPref.getIsDarkTheme()) {
//            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
//            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_white));
//            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_white));
//            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_white));
//        } else {
//            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
//            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//        }

//        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
//        LinearLayout btnShare = view.findViewById(R.id.btn_share);
//        LinearLayout btnReport = view.findViewById(R.id.btn_report);
//
//        btnFavorite.setOnClickListener(v -> {
//            List<Video> videos = dbFavorite.getFavRow(video.vid);
//            if (videos.size() == 0) {
//                dbFavorite.addToFavorite(new Video(
//                        video.category_name,
//                        video.vid,
//                        video.video_title,
//                        video.video_url,
//                        video.video_id,
//                        video.video_thumbnail,
//                        video.video_duration,
//                        video.video_description,
//                        video.video_type,
//                        video.total_views,
//                        video.date_time
//                ));
//                Snackbar.make(parentView, activity.getString(R.string.msg_favorite_added), Snackbar.LENGTH_SHORT).show();
//                imgFavorite.setImageResource(R.drawable.ic_fav);
//
//            } else {
//                if (videos.get(0).vid.equals(video.vid)) {
//                    dbFavorite.RemoveFav(new Video(video.vid));
//                    Snackbar.make(parentView, activity.getString(R.string.msg_favorite_removed), Snackbar.LENGTH_SHORT).show();
//                    imgFavorite.setImageResource(R.drawable.ic_fav_outline);
//                }
//            }
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnShare.setOnClickListener(v -> {
//            shareContent(activity, video.video_title, activity.getResources().getString(R.string.share_text));
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnReport.setOnClickListener(v -> {
//            String str;
//            try {
//                str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:"));
//                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{activity.getString(R.string.report_email)});
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Report " + video.video_title + " channel issue in " + activity.getResources().getString(R.string.app_name));
//                intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
//                        Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
//                        "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Message : ");
//                try {
//                    activity.startActivity(Intent.createChooser(intent, activity.getResources().getString(R.string.menu_report)));
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//                }
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            mBottomSheetDialog.dismiss();
//        });
//
//        if (sharedPref.getIsDarkTheme()) {
//            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
//        } else {
//            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
//        }
//        this.mBottomSheetDialog.setContentView(view);
//
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
//
//        dbFavorite = new DbFavorite(activity);
//        List<Video> videos = dbFavorite.getFavRow(video.vid);
//        if (videos.size() == 0) {
//            txtFavorite.setText(activity.getString(R.string.favorite_add));
//            imgFavorite.setImageResource(R.drawable.ic_fav_outline);
//        } else {
//            if (videos.get(0).vid.equals(video.vid)) {
//                txtFavorite.setText(activity.getString(R.string.favorite_remove));
//                imgFavorite.setImageResource(R.drawable.ic_fav);
//            }
//        }
//
//    }

//    public static boolean isVpnConnectionAvailable() {
//        String iface = "";
//        try {
//            for (NetworkInterface networkInst : Collections.list(NetworkInterface.getNetworkInterfaces())) {
//                if (networkInst.isUp())
//                    iface = networkInst.getName();
//                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
//                    return true;
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

//    public static void showWarningDialog(Activity activity, String title, String message) {
//        new MaterialAlertDialogBuilder(activity)
//                .setTitle(title)
//                .setMessage(message)
//                .setPositiveButton(activity.getString(R.string.dialog_ok), (dialog, which) -> activity.finish())
//                .setCancelable(false)
//                .show();
//    }

//    public static void shareApp(Activity activity) {
//        String message = activity.getString(R.string.share_text) + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
//        sendIntent.setType("text/plain");
//        activity.startActivity(sendIntent);
//    }
//
//    public static void shareContent(Activity activity, String title, String content) {
//        String message = title + "\n\n" + content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
//        sendIntent.setType("text/plain");
//        activity.startActivity(sendIntent);
//    }
//
//    public static void shareContentDetail(Activity activity, String title, String content) {
//        String message = title + "\n\n" + content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
//        sendIntent.setType("text/plain");
//        activity.startActivity(sendIntent);
//    }


    public static void rateUs(Activity activity) {
        String rateUrl = "https://play.google.com/store/apps/details?id=" + activity.getPackageName();
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rateUrl)));
    }

    public static void moreApps(Activity activity, String moreAppsUrl) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(moreAppsUrl)));
    }

//    public static void showAboutDialog(Activity activity) {
//        LayoutInflater layoutInflater = LayoutInflater.from(activity);
//        View view = layoutInflater.inflate(R.layout.dialog_about, null);
//        TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
//        txtAppVersion.setText(activity.getString(R.string.title_settings_version) + " " + BuildConfig.VERSION_NAME);
//        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity);
//        alert.setView(view);
//        alert.setPositiveButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss());
//        alert.show();
//    }

//    public static void postDelayed(OnCompleteListener onCompleteListener, int millisecond) {
//        new Handler(Looper.getMainLooper()).postDelayed(onCompleteListener::onComplete, millisecond);
//    }


    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreenMode(AppCompatActivity activity, boolean show) {
        SharedPref sharedPref = new SharedPref(activity);
        if (show) {
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            int resultColor;
            if (sharedPref.getIsDarkTheme()) {
                //resultColor = ColorUtils.blendARGB(Color.BLACK, ContextCompat.getColor(activity, R.color.color_dark_status_bar), 0.6f);
            } else {
                resultColor = ColorUtils.blendARGB(Color.BLACK, ContextCompat.getColor(activity, R.color.color_light_status_bar), 0.6f);
            }
            //activity.getWindow().setStatusBarColor(resultColor);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }



    public static RequestBuilder<Drawable> RequestBuilder(Context context) {
        return Glide.with(context).asDrawable().sizeMultiplier(0.3f);
    }


//    public void showBottomSheetDialogMoreOptions(View parentView, Post post, boolean isDetailView, OnCompleteListener onCompleteListener) {
//        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.dialog_more_options, null);
//        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
//
//        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
//        ImageView imgShare = view.findViewById(R.id.img_share);
//        ImageView imgReport = view.findViewById(R.id.img_report);
//        ImageView imgFeedback = view.findViewById(R.id.img_feedback);
//        ImageView btnClose = view.findViewById(R.id.btn_close);
//
//        if (sharedPref.getIsDarkTheme()) {
//            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_sheet_dark));
//            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
//            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
//            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
//            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
//            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
//        } else {
//            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_sheet_default));
//            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
//        }
//
//        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
//        LinearLayout btnShare = view.findViewById(R.id.btn_share);
//        LinearLayout btnReport = view.findViewById(R.id.btn_report);
//        LinearLayout btnFeedback = view.findViewById(R.id.btn_feedback);
//
//        if (!sharedPref.getLoginFeature().equals("yes")) {
//            btnReport.setVisibility(View.GONE);
//            btnFeedback.setVisibility(View.GONE);
//        }
//
//        List<Post> posts = dbHandler.getFavRow(post.nid);
//        if (posts.size() == 0) {
//            imgFavorite.setImageResource(R.drawable.ic_favorite_outline_white);
//            ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_add));
//        } else {
//            if (posts.get(0).nid == post.nid) {
//                imgFavorite.setImageResource(R.drawable.ic_favorite_white);
//                ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_remove));
//            }
//        }
//
//        btnFavorite.setOnClickListener(v -> {
//            if (isDetailView) {
//                ((ActivityPostDetails) activity).onFavoriteClicked(post);
//            } else {
//                List<Post> _posts = dbHandler.getFavRow(post.nid);
//                if (_posts.size() == 0) {
//                    dbHandler.AddtoFavorite(new Post(
//                            post.nid,
//                            post.news_title,
//                            post.category_name,
//                            post.news_date,
//                            post.news_image,
//                            post.news_description,
//                            post.content_type,
//                            post.video_url,
//                            post.video_id,
//                            post.comments_count
//                    ));
//                    Snackbar.make(parentView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
//                    imgFavorite.setImageResource(R.drawable.ic_favorite_white);
//                } else {
//                    if (_posts.get(0).nid == post.nid) {
//                        dbHandler.RemoveFav(new Post(post.nid));
//                        Snackbar.make(parentView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
//                        imgFavorite.setImageResource(R.drawable.ic_favorite_outline_white);
//                        onCompleteListener.onComplete();
//                    }
//                }
//            }
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnShare.setOnClickListener(v -> {
//            Tools.shareContentDetail(activity, post);
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnReport.setOnClickListener(v -> {
////            if (sharedPref.getIsLogin()) {
////                String[] items = activity.getResources().getStringArray(R.array.dialog_report_post);
////                singleChoiceSelected = items[0];
////                int itemSelected = 0;
////                new MaterialAlertDialogBuilder(activity)
////                        .setTitle(activity.getString(R.string.txt_dialog_title_report_post))
////                        .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
////                        .setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
////                            Tools.sendReport(activity, "post", String.valueOf(post.nid), "0", sharedPref.getUserId(), "0", "0", post.news_title, singleChoiceSelected);
////                            dialogInterface.dismiss();
////                        })
////                        .setNegativeButton(R.string.dialog_cancel, null)
////                        .show();
////            } else {
////                activity.startActivity(new Intent(activity, ActivityUserLogin.class));
////            }
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnFeedback.setOnClickListener(v -> {
//            if (sharedPref.getIsLogin()) {
//                LayoutInflater inflater = LayoutInflater.from(activity);
//                @SuppressLint("InflateParams") View feedbackView = inflater.inflate(R.layout.custom_dialog_feedback, null);
//
//                LinearLayout toolbar = feedbackView.findViewById(R.id.header_feedback);
//                if (sharedPref.getIsDarkTheme()) {
//                    toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_dark_toolbar));
//                } else {
//                    toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_light_primary));
//                }
//
//                EditText edtFeedback = feedbackView.findViewById(R.id.edt_feedback);
//                TextView txtInfo = feedbackView.findViewById(R.id.txt_info);
//                Button btnSendFeedback = feedbackView.findViewById(R.id.btn_send_feedback);
//                ImageButton btnDismiss = feedbackView.findViewById(R.id.btn_dismiss);
//
//                final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity);
//                alert.setView(feedbackView);
//
//                final AlertDialog alertDialog = alert.create();
//                btnSendFeedback.setOnClickListener(view1 -> {
////                    String msgFeedback = edtFeedback.getText().toString();
////                    if (!msgFeedback.equals("")) {
////                        Tools.postDelayed(() -> {
////                            Tools.sendFeedback(activity, "feedback", String.valueOf(post.nid), "0", sharedPref.getUserId(), "0", "0", post.news_title, msgFeedback);
////                        }, 100);
////                        Tools.hideKeyboard(activity);
////                        alertDialog.dismiss();
////                    } else {
////                        txtInfo.setText(activity.getString(R.string.msg_complete_form));
////                        txtInfo.setVisibility(View.VISIBLE);
////                    }
//                });
//                btnDismiss.setOnClickListener(view1 -> new Handler().postDelayed(alertDialog::dismiss, 200));
//                alertDialog.setCancelable(false);
//                alertDialog.show();
//            } else {
//                // activity.startActivity(new Intent(activity, ActivityUserLogin.class));
//            }
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());
//
//        if (AppConfig.ENABLE_RTL_MODE) {
//            if (sharedPref.getIsDarkTheme()) {
//                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDarkRtl);
//            } else {
//                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLightRtl);
//            }
//        } else {
//            if (sharedPref.getIsDarkTheme()) {
//                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
//            } else {
//                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
//            }
//        }
//        this.mBottomSheetDialog.setContentView(view);
//
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
//
//    }

    public static String getFormatedDate(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

//    public static void setNavigation(Activity activity) {
//        SharedPref sharedPref = new SharedPref(activity);
//        if (sharedPref.getIsDarkTheme()) {
//            darkNavigation(activity);
//        } else {
//            lightNavigation(activity);
//        }
//        getLayoutDirection(activity);
//    }

    public static void getLayoutDirection(Activity activity) {
        if (AppConfig.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

//    public static void setupAppDetailBarLayout(AppCompatActivity activity) {
//        ViewStub viewStub = activity.findViewById(R.id.header_view);
//        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
//            viewStub.setLayoutResource(R.layout.include_appbar_detail_light);
//        } else {
//            viewStub.setLayoutResource(R.layout.include_appbar_detail_default);
//        }
//        viewStub.inflate();
//    }



}