package com.iva.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreen extends Activity {

	/**
	 * @uml.property  name="voice_"
	 * @uml.associationEnd  
	 */
	TextView voice_;
	/**
	 * @uml.property  name="number_"
	 * @uml.associationEnd  
	 */
	EditText number_;
	
	SharedPreferences pref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lockscreen);
		
		pref = getSharedPreferences(getResources().getString(R.string.save), MODE_MULTI_PROCESS);
	 	registerReceiver(new Lock_Screen_Kill(), new IntentFilter("Lock_Screen_release"));
		
		voice_ = (TextView) findViewById(R.id.voice);
		voice_.setText(pref.getString("Voice_password", "err"));
		
		number_ = (EditText) findViewById(R.id.number);
		
		findViewById(R.id.sure).setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				if(pref.getString("Number_password", "err").equals(number_.getText().toString()))
					sendBroadcast(new Intent("Lock_Screen_release"));
				else
					Toast.makeText(LockScreen.this, "비밀번호가 올바르지 않습니다", Toast.LENGTH_LONG).show();
			} 
		}); 
	}
	   
	 @Override
	    public void onBackPressed() {
	    	// TODO Auto-generated method stub
	    	//super.onBackPressed();
	    }
	     
	@Override
    public void onDestroy() {
    	super.onDestroy();
        
    	try {
			unregisterReceiver(new Lock_Screen_Kill());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
    }
	
	class Lock_Screen_Kill extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		finish();
       	}
    }
}
