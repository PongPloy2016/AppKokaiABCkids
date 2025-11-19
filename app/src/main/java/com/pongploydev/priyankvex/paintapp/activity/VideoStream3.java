package com.pongploydev.priyankvex.paintapp.activity;



import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.pongploydev.priyankvex.paintapp.confixdata.File_Confix_Data;
import com.pongploydev.priyankvex.paintapp.R;

public class VideoStream3 extends Activity{

	private int[] play = new int[] {
			R.raw.n0, R.raw.n1,
			R.raw.n2, R.raw.n3, R.raw.n4, R.raw.n5,
			R.raw.n6, R.raw.n7, R.raw.n8, R.raw.n9
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
		VideoStream3.this.finish();
	}
}
