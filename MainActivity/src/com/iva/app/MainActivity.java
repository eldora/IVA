package com.iva.app;

import android.os.Bundle;
import android.app.Activity;
import android.content.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    /**
	 * @uml.property  name="imageView"
	 * @uml.associationEnd  
	 */
    ImageView imageView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		imageView =  (ImageView)findViewById(R.layout.activity_main);
		Toast.makeText(MainActivity.this, "음성인식 시작", Toast.LENGTH_LONG).show();
		new Thread(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				try{
					Thread.sleep(1500);	
				}catch(Throwable ex){
					ex.printStackTrace();
				}
				Intent intent = new Intent(MainActivity.this, Tab_Activity.class);
				startActivity(intent);
				finish();
			}
  
		}).start();
	}

}