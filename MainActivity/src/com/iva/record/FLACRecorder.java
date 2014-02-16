/**
 * This file is part of Audioboo, an android program for audio blogging.
 * Copyright (C) 2011 Audioboo Ltd. All rights reserved.
 *
 * Author: Jens Finkhaeuser <jens@finkhaeuser.de>
 *
 * $Id$
 **/

package com.iva.record;

import android.os.Handler;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Locale;

import fm.audioboo.jni.FLACStreamEncoder;

import android.util.Log;

/**
 * Records a single FLAC file from the microphone. Overwrites the file if it
 * already exists.
 **/
public class FLACRecorder extends Thread {
	/***************************************************************************
	 * Public constants
	 **/
	// Message codes - XXX Also see BooRecorder
	public static final int MSG_OK = 0;
	public static final int MSG_INVALID_FORMAT = 1;
	public static final int MSG_HARDWARE_UNAVAILABLE = 2;
	public static final int MSG_ILLEGAL_ARGUMENT = 3;
	public static final int MSG_READ_ERROR = 4;
	public static final int MSG_WRITE_ERROR = 5;
	public static final int MSG_AMPLITUDES = 6;

	/***************************************************************************
	 * Private constants
	 **/
	// Log ID
	private static final String LTAG = "FLACRecorder";

	/***************************************************************************
	 * Simple class for reporting measured Amplitudes to user of FLACRecorder
	 **/
	public static class Amplitudes {
		public long mPosition;
		public float mPeak;
		public float mAverage;

		public Amplitudes() {
		}

		public Amplitudes(Amplitudes other) {
			mPosition = other.mPosition;
			mPeak = other.mPeak;
			mAverage = other.mAverage;
		}

		public String toString() {
			return String.format(Locale.US, "%dms: %f/%f", mPosition, mAverage,
					mPeak);
		}

		public void accumulate(Amplitudes other) {
			// Position is simple; the overall position is the sum of
			// both positions.
			long oldPos = mPosition;
			mPosition += other.mPosition;

			// The higher peak is the overall peak.
			if (other.mPeak > mPeak) {
				mPeak = other.mPeak;
			}

			// Average is more complicated, because it needs to be weighted
			// on the time it took to calculate it.
			float weightedOld = mAverage * (oldPos / (float) mPosition);
			float weightedNew = other.mAverage
					* (other.mPosition / (float) mPosition);
			mAverage = weightedOld + weightedNew;
		}
	}

	/***************************************************************************
	 * Public data
	 **/
	
	

	/***************************************************************************
	 * Private data
	 **/
	// 
	private ArrayList<Integer> mDeviceAudioRecordSettingValue;
	
	// Flag that keeps the thread running when true.
	private boolean mShouldRun;
	
	// Flag that signals whether the thread should record or ignore PCM data.
	private boolean mShouldRecord = false;

	// Stream encoder
	private FLACStreamEncoder mEncoder;

	// File path for the output file.
	private String mPath;

	// Handler to notify at the above report interval
	private Handler mHandler;

	// Remember the duration of the recording. This is in msec.
	private double mDuration;

	/***************************************************************************
	 * Implementation
	 **/
	public FLACRecorder(ArrayList<Integer> settingValue, String path, Handler handler) {
		mDeviceAudioRecordSettingValue = settingValue;
		mPath = path;
		mHandler = handler;
		
		Log.d(LTAG, "New FLACRecorder, path: " + mPath);
	}

	public void startRecording() {
		mShouldRecord = true;
	}

	public void stopRecording(){
		mShouldRecord = false;
	}

	public boolean isRecording() {
		return mShouldRun && mShouldRecord;
	}

	public double getDuration() {
		// Duration for Boos is normally in secs, and we're remembering msecs
		// here,
		// so we'll need to convert.
		return mDuration / 1000;
	}

	public Amplitudes getAmplitudes() {
		if (null == mEncoder) {
			return null;
		}

		Amplitudes amp = new Amplitudes();
		amp.mPosition = (long) mDuration;
		amp.mPeak = mEncoder.getMaxAmplitude();
		amp.mAverage = mEncoder.getAverageAmplitude();

		return amp;
	}

	public static int mapChannelConfig(int channelConfig) {
		switch (channelConfig) {
		case AudioFormat.CHANNEL_CONFIGURATION_MONO:
			return 1;

		case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
			return 2;

		default:
			return 0;
		}
	}

	public static int mapFormat(int format) {
		switch (format) {
		case AudioFormat.ENCODING_PCM_8BIT:
			return 8;

		case AudioFormat.ENCODING_PCM_16BIT:
			return 16;

		default:
			return 0;
		}
	}

	public void run() {
		// Determine audio config to use.
		int sample_rate = mDeviceAudioRecordSettingValue.get(0);
		int channel_config = mDeviceAudioRecordSettingValue.get(1);
		int format = mDeviceAudioRecordSettingValue.get(2);
		int bufsize = mDeviceAudioRecordSettingValue.get(3);
		AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sample_rate, channel_config, format, bufsize);
		
		Log.d(LTAG, "Using: " + format + "/" + channel_config + "/" + sample_rate);

		mShouldRun = true;
		boolean oldShouldRecord = false;

		try {
			// Initialize variables for calculating the recording duration.
			int mapped_format = mapFormat(format);
			int mapped_channels = mapChannelConfig(channel_config);
			int bytesPerSecond = sample_rate * (mapped_format / 8)
					* mapped_channels;

			// Set up encoder. Create path for the file if it doesn't yet exist.
			Log.d(LTAG, "Setting up encoder " + mPath + " rate: " + sample_rate
					+ " channels: " + mapped_channels + " format "
					+ mapped_format);

			mEncoder = new FLACStreamEncoder(mPath, sample_rate,
					mapped_channels, mapped_format);

			// Start recording loop
			mDuration = 0.0;
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufsize);
			while (mShouldRun) {
				// Toggle recording state, if necessary
				if (mShouldRecord != oldShouldRecord) {
					// State changed! Let's see what we are supposed to do.
					if (mShouldRecord) {
						Log.d(LTAG, "Start recording!");
						recorder.startRecording();
					} else {
						Log.d(LTAG, "Stop recording!");
						recorder.stop();
						mEncoder.flush();
						mShouldRun = false;
					}
					oldShouldRecord = mShouldRecord;
				}

				// If we're supposed to be recording, read data.
				if (mShouldRecord) {
					int result = recorder.read(buffer, bufsize);
					switch (result) {
					case AudioRecord.ERROR_INVALID_OPERATION:
						Log.e(LTAG, "Invalid operation.");
						mHandler.obtainMessage(MSG_READ_ERROR).sendToTarget();
						break;

					case AudioRecord.ERROR_BAD_VALUE:
						Log.e(LTAG, "Bad value.");
						mHandler.obtainMessage(MSG_READ_ERROR).sendToTarget();
						break;

					default:
						if (result > 0) {
							// Compute time recorded
							double read_ms = (1000.0 * result) / bytesPerSecond;
							mDuration += read_ms;

							int write_result = mEncoder.write(buffer, result);
							if (write_result != result) {
								Log.e(LTAG, "Attempted to write " + result + " but only wrote " + write_result);
								mHandler.obtainMessage(MSG_WRITE_ERROR).sendToTarget();
							} else {
								Amplitudes amp = getAmplitudes();
								mHandler.obtainMessage(MSG_AMPLITUDES, amp).sendToTarget();
							}
						}
					}
				}
			}

			recorder.release();
			mEncoder.release();
			mEncoder = null;

		} catch (IllegalArgumentException ex) {
			Log.e(LTAG, "Illegal argument: " + ex.getMessage());
			mHandler.obtainMessage(MSG_ILLEGAL_ARGUMENT, ex.getMessage())
					.sendToTarget();
		}

		mHandler.obtainMessage(MSG_OK).sendToTarget();
	}
}
