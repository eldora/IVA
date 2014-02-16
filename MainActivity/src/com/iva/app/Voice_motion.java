package com.iva.app;

import java.io.*;
import java.net.*;
import java.util.*;

import android.R.string;
import android.bluetooth.*;
import android.content.*;
import android.content.IntentSender.SendIntentException;
import android.database.*;
import android.media.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.telephony.*;
import android.util.*;
import android.view.KeyEvent;
import android.widget.*;

public class Voice_motion {
	private String ip = "192.168.0.62"; // IP
	private int port = 5090; // PORT번호
	SharedPreferences pref;
	Cursor cursor;
	Context context;
	characterFilter filter;


	public Voice_motion(Context context, SharedPreferences pref){
		this.context =  context;
		this.pref = pref;
		filter = new characterFilter(context, pref);
		StrictMode.ThreadPolicy policy = new 
				StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

	}

	public void receiveResults(ArrayList<String> results) {

		String filter_result =filter.setReceive_result(results);
		if(filter_result != null){
			Log.e("lll", filter_result);
			String[] result_of_filter = filter_result.split(" ");
 
			if(result_of_filter[0].equals("통화")){
				Call_value(result_of_filter[1]);
			}else if(result_of_filter[0].equals("음악")){
				Music_value(result_of_filter[1]);
			}else if(result_of_filter[0].equals("문자")){
				Message_value(result_of_filter);
			}else if(result_of_filter[0].equals("검색")){
				Search_value(result_of_filter[1]);
			}else if(result_of_filter[0].equals("무음")){
				slient_vibrate(0);
			}else if(result_of_filter[0].equals("벨소리")){
				slient_vibrate(2);
			}else if(result_of_filter[0].equals("진동")){
				slient_vibrate(1);
			}else if(result_of_filter[0].equals("받기")){
				receive_call();
			}else if(result_of_filter[0].equals("거절")){
				reject_call();
			}
			else if(result_of_filter[0].equals("와이파이")){
				wifi_on_off();
			}
			else if(result_of_filter[0].equals("인식")){
				Voice_value("인식"); 
			}else if(result_of_filter[0].equals("실행")){
				Voice_value(result_of_filter[1]);
			}else if(result_of_filter[0].equals("터치")){
				touch_soc(result_of_filter[1]);
			}else if(result_of_filter[0].equals("버튼")){
				Voice_value(result_of_filter[1]);
			}else if(result_of_filter[0].equals("스크롤")){
				Voice_value(result_of_filter[0]+" " + result_of_filter[1]);
			}
			
			else
				lock_release(filter_result);
		}
	}

	public void slient_vibrate(final int i){
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				aManager.setRingerMode(i);
			}
		}).start();

	}

	public void receive_call(){
		TelephonyManager manager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
		if(manager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
			new Thread(new Runnable(){
				public void run(){
					Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
					buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
					context.sendOrderedBroadcast(buttonUp, null);

					buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
					context.sendOrderedBroadcast(buttonUp, null);
				}
			}).start();   

	}

	public void reject_call(){
		TelephonyManager manager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
		if(manager.getCallState() == TelephonyManager.CALL_STATE_RINGING)
			new Thread(new Runnable(){
				public void run(){
					Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
					buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
					context.sendOrderedBroadcast(buttonUp, null);
				}
			}).start(); 
	}

	public void wifi_on_off(){
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				String service = Context.WIFI_SERVICE;
				final WifiManager wifi = (WifiManager)context.getSystemService(service);

				if(!wifi.isWifiEnabled()){
					if(wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
						wifi.setWifiEnabled(true);
						Log.e("test", "error");
					}
				}else {
					wifi.setWifiEnabled(false);
				}
			}
		}).start();

	}

	public void lock_release(String str){
		if(str.contains(pref.getString("Voice_password", "err"))){
			context.sendBroadcast(new Intent("Lock_Screen_release"));
		}
	}

	//소켓통신
	private void Socket_Communication(String Voice_value){
		try {
			Socket socket = new Socket(ip, port);
			BufferedWriter networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			PrintWriter out = new PrintWriter(networkWriter, true);
			String return_msg = Voice_value;
			out.println(return_msg);

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void touch_soc(String v){
		Socket_Communication(v);
	}

	//음성인식 결과값으로 어플 및 버튼 실행   
	public void Voice_value(String Voice_value){
		String[] Buttons = {"HOME","BACK","MENU","POWER","VOLUME_UP","VOLUME_DOWN","SCROLL_UP","SCROLL_DOWN","SCROLL_LEFT","SCROLL_RIGHT","TAKE"};
		List<String> bt = Arrays.asList(Buttons);

		if(bt.contains(pref.getString(Voice_value, "HOME"))){
			Socket_Communication(pref.getString(Voice_value, "HOME"));
		}else{
			Intent intent = this.context.getPackageManager()
					.getLaunchIntentForPackage(pref.getString(Voice_value, "error"));
			context.startActivity(intent); 
		}
	}

	//음성인식 결과 값으로 전화 걸기
	private void Call_value(String result_List) {
		cursor = context.getContentResolver()
				.query(ContactsContract.CommonDataKinds
						.Phone.CONTENT_URI, null, ContactsContract
						.CommonDataKinds.Phone.DISPLAY_NAME +" = ?"
						,new String[]{result_List}, null);

		cursor.moveToFirst();
		String phone_name = cursor.getString(cursor.getColumnIndex
				(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		Log.e("cursor", phone_name);
		String phone_number = cursor.getString
				(cursor.getColumnIndex(ContactsContract
						.CommonDataKinds.Phone.NUMBER));

		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + phone_number));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(intent);

	}

	//음성인식 결과값으로 음악 실행하기 
	private void Music_value(String result_List) {
	
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse
					("content://media/external/audio/media/" + result_List), "audio/mp3");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);

	}

	//음성인식 결과값으로 문자 보내기
	private void Message_value(String[] result_List) {
		String message_content="";

		if(result_List.length != 0){
			
			cursor = context.getContentResolver().query(ContactsContract
					.CommonDataKinds.Phone.CONTENT_URI, null
					,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?"
					,new String[]{result_List[1]}, null);	

			for(int i = 2; i < result_List.length;i++)
				message_content += result_List[i]+ " ";

			cursor.moveToFirst();

			String phone_number = cursor.getString(cursor.getColumnIndex
					(ContactsContract.CommonDataKinds.Phone.NUMBER));
			Log.e("phone", phone_number);

			SmsManager manager = SmsManager.getDefault();
			String sendTo = phone_number;
			manager.sendTextMessage(sendTo, null, message_content, null, null);
		}
	}


	//음성인식 결과 값으로 웹 검색 실행 하기
	private void Search_value(String heard) {

		Uri uri = Uri.parse("http://search.nate.com/search/all.html?s=&sc=&afc=&j=&thr" +
				"=sbma&nq=&q=" + heard + "&x=0&y=0");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

	}
}
