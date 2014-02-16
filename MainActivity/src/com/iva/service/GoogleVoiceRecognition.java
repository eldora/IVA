package com.iva.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class GoogleVoiceRecognition extends AsyncTask<Object, Void, String> {
	private static final String TAG = "GoogleVoiceRecognition";
	public static final int MESSAGE_TAG = 200;
	
	private static final String staticGoogleVoiceRecognitionUrl = 
			"https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium";
	
	private HttpClient client;
	private Handler handler;
	private String flacFile;
	
	private String googleVoiceRecognitionUrl;
	private String language;
	private int maxResult;

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		language = "ko-KR";
		maxResult = 15;
		googleVoiceRecognitionUrl = staticGoogleVoiceRecognitionUrl + ("&lang=" + language + "&maxresults=" + maxResult);
		
		client = new DefaultHttpClient();
	}

	@Override
	protected String doInBackground(Object... params) {
		// TODO Auto-generated method stub
		this.handler = (Handler)params[0];
		flacFile = (String)params[1];
		
		HttpPost p = new HttpPost(googleVoiceRecognitionUrl);
		p.addHeader("Content-Type", "audio/x-flac; rate=44100");
		p.setEntity(new FileEntity(new File(flacFile), "audio/x-flac; rate=44100"));

		try {
			HttpResponse response = client.execute(p);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			String str = null;

			while ((str = reader.readLine()) != null) {
				sb.append(str + '\n');
			}
			reader.close();

			if (response.getStatusLine().getStatusCode() == 200) {
				return sb.toString();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);

		ArrayList<String> result_array = extractJsonData(result);

		if(result_array.isEmpty())
			Log.e(TAG, "result_Array return value is null");
		else{
			Log.e(TAG, "result_Array : " + result_array.toString());	//result
			sendMessage(result_array);
		}
	}

	private ArrayList<String> extractJsonData(String feed){
		

		ArrayList<String> result = new ArrayList<String>();
		if(feed != null){
			feed = feed.trim();
			try {
				JSONObject jsonObject =new JSONObject(feed);
				
				for(int i =0;i<jsonObject.getJSONArray("hypotheses").length();i++){
					JSONObject hypotheses = jsonObject.getJSONArray("hypotheses").getJSONObject(i);
					result.add(hypotheses.getString("utterance"));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "JSON ERROR");
			}			
		}
		
		return result;
	}
	
	private void sendMessage(ArrayList<String> result){
		handler.obtainMessage(MESSAGE_TAG, result).sendToTarget();
	}
}