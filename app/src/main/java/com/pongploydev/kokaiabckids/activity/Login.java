package com.pongploydev.kokaiabckids.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pongploydev.kokaiabckids.R;
import com.pongploydev.kokaiabckids.sqlite.Databaseregister;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Adil Soomro
 * 
 */
public class Login extends Activity {
	Databaseregister databaserejister;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		databaserejister=new Databaseregister(this);
		final EditText user = (EditText) findViewById(R.id.editText1);
		final EditText pass = (EditText) findViewById(R.id.editText2);
        
        		  Button login = (Button) findViewById(R.id.button21);
	                  login.setOnClickListener(new OnClickListener() {
        		            public void onClick(View v) {
        		                // TODO Auto-generated method stub 
        		            	if(user.getText().toString().equals("")||pass.getText().toString().equals("")){
        		            		Toast.makeText(Login.this," กรุณากรอกข้อมูล ", Toast.LENGTH_LONG).show();
        		            		
        		            	}else{
        		            		String namecode="";
        		            		ArrayList<HashMap<String, String>> SelectAllData= databaserejister.SelectAllData();
        		            		
        		            		boolean checkT=false;
        		            		for(int i=0;i<SelectAllData.size();i++){
        		            			if(SelectAllData.get(i).get("col1").equals(user.getText().toString())&&SelectAllData.get(i).get("col2").equals(pass.getText().toString())){
        		            				
        		            				namecode=SelectAllData.get(i).get("Code");
        		            				checkT=true;
        		            			}
        		            		}
        		            		 if(checkT){

        		            			            			
        		            		 }else{
        		            		 Toast.makeText(Login.this,"  username หรือ password ผิด ", Toast.LENGTH_LONG).show();
        		                   }
        		            		 
        			            	 
        		            	}
        		            } 
        		        }); 
        			 
        			 
	  Button register = (Button) findViewById(R.id.register);
		 register.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // TODO Auto-generated method stub 
	            	Intent i=new Intent(Login.this,Register.class);
	            	 startActivity(i);
	            } 
	        }); 
	}
	@Override
	public void onBackPressed() {
 

        		Login.this.finish();

             
}
}