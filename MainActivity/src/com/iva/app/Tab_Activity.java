package com.iva.app;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class Tab_Activity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.voice_tab);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;



		intent = new Intent(this, Tab1_Start.class);
		spec = tabHost.newTabSpec("tab1").setIndicator("음성인식", res.getDrawable(R.drawable.voice)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, Tab2_Applist.class);
		spec = tabHost.newTabSpec("tab2").setIndicator("어플목록", res.getDrawable(R.drawable.app)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, Tab3_Motion_list.class);
		spec = tabHost.newTabSpec("tab3").setIndicator("동작목록", res.getDrawable(R.drawable.memo)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, Tab4_PassWord.class);
		spec = tabHost.newTabSpec("tab4").setIndicator("비밀번호", res.getDrawable(R.drawable.security)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);

	}
}