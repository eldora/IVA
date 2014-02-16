package com.iva.service;

import java.util.ArrayList;

import com.iva.app.*;
import com.iva.record.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/*
 *  10초 뒤에 들어오는 메시지가 있다 그 값이 10000을 넘어버리기 때문에 restartrecord가 계속 호출되는 문제 - 메시지큐를 플레시하는 방법을 찾아바라 
 */

public class VoiceRecognitionService extends Service {
	private static final String TAG = "VoiceRecognitionService";
	private static final int SERVICE_ID = 1;

	private VoiceRecorder    mRecorder;
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	private NotificationCompat.Builder builder;
	private NotificationManager mNotificationManager;
	private Notification notification_start, notification_recording1, notification_recording2, notification_recording3;

	private boolean serviceFlag, speakedFlag;
	private float amplitudeCriterion;						// 이 값 이상의 amplitude가 들어오면 음성으로 간주 그 이하는 잡음 
																// amplitudeStorage.getAverageAmplitude을 통해 amplitudeCriterion 간격마다 동기화
	
	private long speakedDelayTime;							// 말한 후 말이 끝났는지 기다리는 시간
	private long defaultRecordingTime;						// 이 시간이 최대녹음시간이다
	
	private long oldPosition;
	private Voice_motion voice_motion;
	
	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case FLACRecorder.MSG_AMPLITUDES:
				FLACRecorder.Amplitudes amp = (FLACRecorder.Amplitudes) msg.obj;
				// updateRecordingState(amp);
				
				if(amp.mPeak < amplitudeCriterion)
					mNotificationManager.notify(SERVICE_ID, notification_start);
				else if(amp.mPeak < amplitudeCriterion*2)
					mNotificationManager.notify(SERVICE_ID, notification_recording1);
				else if(amp.mPeak < amplitudeCriterion*3)
					mNotificationManager.notify(SERVICE_ID, notification_recording2);
				else
					mNotificationManager.notify(SERVICE_ID, notification_recording3);
				
				if(amp.mPeak > amplitudeCriterion){
					speakedFlag = true;
					oldPosition = amp.mPosition;
					Log.i(TAG, "Amp = " + amp.mAverage + ", " + amp.mPeak + ", " + amp.mPosition);
					//mNotificationManager.notify(SERVICE_ID, notification_recording1);
				}
				
				if((speakedFlag&&(amp.mPosition>oldPosition+speakedDelayTime)) ||
						(amp.mPosition>oldPosition+defaultRecordingTime)){
					restartRecord();					
				}
				
				break;

			case FLACRecorder.MSG_OK:
				// Ignore
				Log.i(TAG, "MSG_OK");
				break;

			case VoiceRecorder.MSG_END_OF_RECORDING:
				// updateButtons();
				Log.i(TAG, "MSG_END_OF_RECORDING");
				oldPosition = 0;
				break;
				
			case GoogleVoiceRecognition.MESSAGE_TAG:
				ArrayList<String> result = (ArrayList<String>)msg.obj;
				voice_motion.receiveResults(result);
				Log.e(TAG, result.toString());	//result
				break;
 
			default:
				break;
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG, "onCreate()");
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(new BroadCastReceiver(), filter);

		
		// 수정할 내용
		SharedPreferences pref = getSharedPreferences(getResources().getString(R.string.save), MODE_MULTI_PROCESS); 
		voice_motion = new Voice_motion(this, pref);
		
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		builder = new NotificationCompat.Builder(this)
		.setContentTitle("VoiceRecognitionService")
		.setContentText("Running")
		.setSmallIcon(R.drawable.mic)
		.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
		notification_start = builder.build();
		
//		builder = new NotificationCompat.Builder(this)
		builder.setSmallIcon(R.drawable.mic1);
		notification_recording1 = builder.build();

//		builder = new NotificationCompat.Builder(this)
		builder.setSmallIcon(R.drawable.mic2);
		notification_recording2 = builder.build();

//		builder = new NotificationCompat.Builder(this)
		builder.setSmallIcon(R.drawable.mic3);
		notification_recording3 = builder.build();
		
		startForeground(SERVICE_ID, notification_start);
		
		HandlerThread mHandlerThread = new HandlerThread("VoiceRecognitionServiceHandler", Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();
		mServiceLooper = mHandlerThread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		mRecorder = new VoiceRecorder(this, mServiceHandler);

		amplitudeCriterion = (float) 0.5;
		
		speakedDelayTime = 1000;
		defaultRecordingTime = 10000;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStartCommand");
		
		serviceFlag = true;
		startRecord();
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");
		
		
		serviceFlag = false;
		stopRecord();
		stopForeground(true);
		
		super.onDestroy();
	}

	public void restartRecord(){
		// 늦게 오는 메시지 처리 때문에
		oldPosition+=1000;
		
		stopRecord();

		if(speakedFlag)
			VoiceRecognition(mRecorder.getFilename());
		else
			mRecorder.getFilename();
		
		if(serviceFlag)
			startRecord();
	}
	
	private void startRecord(){
		Log.i(TAG, "Recording Start");
		
		speakedFlag = false;
		
		mRecorder.start();
	}
	
	private void stopRecord(){
		Log.i(TAG, "Recording Stop");
		
		mRecorder.stop(); 
	}

	private void VoiceRecognition(String file){
		new GoogleVoiceRecognition().execute(mServiceHandler, file);
	}
}
