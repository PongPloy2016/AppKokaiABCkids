package com.pongploydev.priyankvex.paintapp;

import android.content.Intent;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.WindowManager;
import android.widget.ImageView;

import com.pongploydev.priyankvex.paintapp.R;
import com.pongploydev.priyankvex.paintapp.activity.MainActivity;


public class SplashScreen extends Activity  {

		private final Handler handler = new Handler();
		private boolean navigated = false;
		private final Runnable navigateRunnable = new Runnable() {
			@Override
			public void run() {
				navigateToMain();
			}
		};

	 
	// private ProgressDialog progressDialog;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.activity_splash);

			ImageView logo = findViewById(R.id.splash_logo);
			if (logo != null) {
				Animation bounceIn = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
				bounceIn.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						if (navigated) {
							return;
						}
						Animation rotate = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.rotate_continuous);
						logo.startAnimation(rotate);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
				});
				logo.startAnimation(bounceIn);
				logo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						navigateToMain();
					}
				});
			}

			ImageView decorStar1 = findViewById(R.id.decor_star1);
			ImageView decorStar2 = findViewById(R.id.decor_star2);
			ImageView decorBrush = findViewById(R.id.decor_brush);
			ImageView decorPalette = findViewById(R.id.decor_palette);

			Animation wiggle = AnimationUtils.loadAnimation(this, R.anim.wiggle);
			if (decorStar1 != null) decorStar1.startAnimation(wiggle);
			if (decorStar2 != null) decorStar2.startAnimation(wiggle);

			Animation tada = AnimationUtils.loadAnimation(this, R.anim.tada);
			if (decorBrush != null) decorBrush.startAnimation(tada);
			if (decorPalette != null) decorPalette.startAnimation(tada);
			View root = findViewById(android.R.id.content);
			if (root != null) {
				root.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						navigateToMain();
					}
				});
			}

			handler.postDelayed(navigateRunnable, 3000);
		}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }

    private void navigateToMain() {
        if (navigated) {
            return;
        }
        navigated = true;
        handler.removeCallbacks(navigateRunnable);
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
