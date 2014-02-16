package com.iva.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class Tab4_PassWord extends Activity {
	
	SharedPreferences preferences;
	SharedPreferences.Editor edit;
	/**
	 * @uml.property  name="voice"
	 * @uml.associationEnd  
	 */
	EditText Voice;
	/**
	 * @uml.property  name="number"
	 * @uml.associationEnd  
	 */
	EditText Number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_lockscreen);
		preferences = getSharedPreferences(getResources().getString(R.string.save)
				,MODE_MULTI_PROCESS); 
		edit = preferences.edit();
		
		Voice = (EditText) findViewById(R.id.edittext_voice_password);
		Number = (EditText) findViewById(R.id.edittext_number_password);
		
		findViewById(R.id.btn_voice_password).setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				edit.putString("Voice_password", Voice.getText().toString());
				edit.commit();
				Voice.setText("");
				
			}
		});
		
		findViewById(R.id.btn_number_password).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				edit.putString("Number_password", Number.getText().toString());
				edit.commit();
				Number.setText("");
				
			}
		});
	}
}