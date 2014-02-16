package com.iva.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
			Start_LockScreen(context);
		}
	}
	
	private void Start_LockScreen(Context context){
		context.startActivity(new Intent(context, LockScreen.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
