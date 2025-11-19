package com.pongploydev.priyankvex.paintapp;

import android.content.Intent;

import android.os.Bundle;
 

import android.app.Activity;

import com.pongploydev.priyankvex.paintapp.R;
import com.pongploydev.priyankvex.paintapp.activity.MainActivity;


public class SplashScreen extends Activity  {

	 
	// private ProgressDialog progressDialog;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_splash);

			new Thread(new Runnable() {
				public void run() {

					try {

						Thread.sleep(3000);
					} catch (InterruptedException e) { }

					Intent i = new Intent(SplashScreen.this, MainActivity.class);
					startActivity(i);
					finish();
				}
			}).start();

 
           } 
			
		 

		 
	 
	}
