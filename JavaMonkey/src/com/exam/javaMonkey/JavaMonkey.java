/**
 * Copyright (C) 2011  Diego Torres Milano
 */
package com.exam.javaMonkey;

import java.util.*;
import com.android.chimpchat.ChimpChat;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;

/**
 * @author diego
 *
 */
public class JavaMonkey {

	private static final String ADB = "C:/CookAndroid/android-sdk/platform-tools/adb";
	private static final long TIMEOUT = 5000;
	private ChimpChat mChimpchat;
	private IChimpDevice mdevice;
	private ArrayList<String> text;
	private ArrayList<String> title;


	/**
	 * Constructor
	 */
	public JavaMonkey() {
		super();
		TreeMap<String, String> options = new TreeMap<String, String>();
		options.put("backend", "adb");
		options.put("adbLocation", ADB);
		mChimpchat = ChimpChat.getInstance(options);
	}

	/**
	 * Initializes the JavaMonkey.
	 */
	void init() {
//		mdevice = mChimpchat.waitForConnection(TIMEOUT, "192.168.0.54:5555");
				mdevice = mChimpchat.waitForConnection(TIMEOUT, ".*");

		if ( mdevice == null ) {
			throw new RuntimeException("Couldn't connect.");
		}
		mdevice.wake();

	}

	/**
	 * List all properties.
	 */
	void listProperties() {
		if ( mdevice == null ) {
			throw new IllegalStateException("init() must be called first.");
		}
		for (String prop: mdevice.getPropertyList()) {
			System.out.println(prop + ": " + mdevice.getProperty(prop));
		}
	}

	/**
	 * Terminates this JavaMonkey.
	 */
	void shutdown() {
		mChimpchat.shutdown();
		mdevice = null;
	}
	void Key_Event(String btn){
		if(btn.equals("KEYCODE_POWER")) {
			mdevice.press("KEYCODE_POWER",TouchPressType.DOWN_AND_UP);
		}
		else if(btn.equals("MENU")){
			mdevice.press("KEYCODE_MENU", TouchPressType.DOWN_AND_UP);
		}
		else if(btn.equals("HOME")){
			mdevice.press("KEYCODE_HOME", TouchPressType.DOWN_AND_UP);	
		}
		else if(btn.equals("BACK")){
			mdevice.press("KEYCODE_BACK", TouchPressType.DOWN_AND_UP);
		}
		else if(btn.equals("POWER")){
			mdevice.press("KEYCODE_POWER", TouchPressType.DOWN_AND_UP);
		}
		else if(btn.equals("VOLUME_UP")){
			mdevice.press("KEYCODE_VOLUME_UP", TouchPressType.DOWN_AND_UP);
		}
		else if(btn.equals("VOLUME_DOWN")){
			mdevice.press("KEYCODE_VOLUME_DOWN", TouchPressType.DOWN_AND_UP);
		}
		else if(btn.equals("SCROLL_UP")){
			mdevice.drag(360, 640, 360, 1000, 20, 25);
		}
		else if(btn.equals("SCROLL_DOWN")){
			mdevice.drag(360,640,360,100, 20,25);
		}
		else if(btn.equals("SCROLL_LEFT")){
			mdevice.drag(360,640,700,640, 20,25);
		}
		else if(btn.equals("SCROLL_RIGHT")){
			mdevice.drag(360,640,60,640, 20,25);
		}else if(btn.equals("TAKE")){
			takeScreenShot();
		}
	}
	
	public void takeScreenShot(){
		IChimpImage image = mdevice.takeSnapshot();
		image.writeToFile("C:/TessImage/tessImg.png","png");
		CharcterRecognizer cr = new CharcterRecognizer("kor");
		cr.recog();
		title = cr.getTitle();
		text = cr.getText();
	}
	
	public void touchxy(int x, int y){
		mdevice.touch(x, y, TouchPressType.DOWN_AND_UP);
	}

	public ArrayList<String> getText() {
		return text;
	}

	public ArrayList<String> getTitle() {
		return title;
	}
}
