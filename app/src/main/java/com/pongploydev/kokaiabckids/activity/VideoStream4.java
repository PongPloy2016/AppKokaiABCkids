package com.pongploydev.kokaiabckids.activity;



import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.pongploydev.kokaiabckids.confixdata.File_Confix_Data;
import com.pongploydev.kokaiabckids.R;

public class VideoStream4 extends Activity{

	private int[] play = new int[] {
			R.raw.v0, R.raw.v1,
			R.raw.v2, R.raw.v3, R.raw.v4, R.raw.v5,
			R.raw.v6, R.raw.v7, R.raw.v8, R.raw.v9
			 };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		VideoView videoView =(VideoView)findViewById(R.id.surface_view);
	    MediaController mediaController= new MediaController(this);
	    mediaController.setAnchorView(videoView);        
	    Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+play[File_Confix_Data.position_id_vdo1 ]);
	    videoView.setMediaController(mediaController);
	    videoView.setVideoURI(uri);        
	    videoView.requestFocus();
	    videoView.start();
	}
	@Override
	public void onBackPressed() {
		VideoStream4.this.finish();
	}
}
