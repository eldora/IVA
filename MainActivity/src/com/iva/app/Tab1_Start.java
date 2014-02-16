package com.iva.app;

import com.iva.service.VoiceRecognitionService;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class Tab1_Start extends Activity {
    /**
	 * @uml.property  name="intent"
	 * @uml.associationEnd  
	 */
    Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_start);

		Toast.makeText(Tab1_Start.this, "음성인식 실행"
				,Toast.LENGTH_SHORT).show();
		intent = new Intent(Tab1_Start.this, VoiceRecognitionService.class);
		
		findViewById(R.id.serviceStart).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startService(intent);
				Log.i("TEST", "START SERVICE");
			}
		});
		
		findViewById(R.id.serviceStop).setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService(intent);
			}
		});
	}
}
