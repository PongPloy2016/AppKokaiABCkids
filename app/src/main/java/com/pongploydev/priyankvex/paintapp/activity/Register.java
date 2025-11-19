package com.pongploydev.priyankvex.paintapp.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pongploydev.priyankvex.paintapp.R;
import com.pongploydev.priyankvex.paintapp.sqlite.Databaseregister;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Adil Soomro
 * 
 */
public class Register extends Activity {
	Databaseregister databaserejister;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		databaserejister=new Databaseregister(this);
		final EditText editText1 = (EditText) findViewById(R.id.editText1);
		final EditText editText2 = (EditText) findViewById(R.id.editText2);
		final EditText editText3 = (EditText) findViewById(R.id.editText3);
		final EditText editText4 = (EditText) findViewById(R.id.editText4);
		final EditText editText5 = (EditText) findViewById(R.id.editText5);
		
		  Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
		  btnSubmit.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // TODO Auto-generated method stub 
	            	if(editText1.getText().toString().equals("")||editText2.getText().toString().equals("")
	            			||editText3.getText().toString().equals("")||editText4.getText().toString().equals("")
	            			||editText5.getText().toString().equals("")){
	            		Toast.makeText(Register.this,"กรุณากรอกข้อมูล ", Toast.LENGTH_LONG).show();
	            		
	            	}else{
	            		ArrayList<HashMap<String, String>> SelectAllData= databaserejister.SelectAllData();
	            		boolean checkT=true;
for(int i=0;i<SelectAllData.size();i++){
	if(SelectAllData.get(i).get("col1").equals(editText1.getText().toString())){
		checkT=false;
		editText1.setText(" ");
		
	}
}
	            		if(checkT){
	            			
	            			databaserejister.InsertData(null, editText1.getText().toString(), editText2.getText().toString(), editText3.getText().toString()
	            			, editText4.getText().toString(), editText5.getText().toString(), " ",  " ",  " ",  " ");
	            			Toast.makeText(Register.this," บันทึกเรียบร้อย", Toast.LENGTH_LONG).show();
	            			Register.this.finish();
	            			
	            		}else{
	            			Toast.makeText(Register.this,"   username นี้มีอยู่แล้ว  ", Toast.LENGTH_LONG).show();
	            		}
	            		
	            		
	            	}	
	            } 
	        }); 
		  
		  
	  Button btnCancel = (Button) findViewById(R.id.btnCancel);
		  
	  btnCancel.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // TODO Auto-generated method stub 
	            	Register.this.finish();
	            } 
	        }); 
		   
	        
	}
}
