package com.iva.record;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.zip.CRC32;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VoiceRecorder {
	/***************************************************************************
	 * Public constants
	 **/
	// Message ID for end of recording; at this point stats are finalized.
	// XXX Note that the message ID must be at least one higher than the highest
	// FLACRecorder message ID.
	public static final int MSG_END_OF_RECORDING = FLACRecorder.MSG_AMPLITUDES + 1;

	/***************************************************************************
	 * Private constants
	 **/
	// Log ID
	private static final String LTAG = "VoiceRecorder";
	
	// 임시 저장할 파일 개수
	private static final int TEMPFILENUMBER = 5;
	
	//
	private static final String DATA_DIR_PREFIX = "boo_data";

	// Serialized file extension.
	public static final String EXTENSION = ".boo";
	// Data dir extension
	public static final String DATA_EXTENSION = ".data";
	// Recording extension
	public static final String RECORDING_EXTENSION = ".rec";

	/***************************************************************************
	 * Private data
	 **/
	// Context
	private WeakReference<Context> mContext;
	
	private ArrayList<Integer> mDeviceAudioRecordSettingValue;

	// Boo to record into. And current Recording
	private Recording mRecording;

	// Handler for messages sent by BooRecorder
	private Handler mUpchainHandler;
	// Internal handler to hand to FLACRecorder.
	private Handler mInternalHandler;

	// For recording FLAC files.
	private FLACRecorder mRecorder;

	// Overall recording metadata
	private FLACRecorder.Amplitudes mAmplitudes;
	private FLACRecorder.Amplitudes mLastAmplitudes;

	//
	private List<Recording> mListRecording;
	
	// 
	private int mListRecordingIndex;
	
	//
	private String mDir;
	
	//
	private Queue<String> mRecordingFile;

	/***************************************************************************
	 * Implementation
	 **/
	public VoiceRecorder(Context context, Handler handler) {
		mContext = new WeakReference<Context>(context);
		mUpchainHandler = handler;
		mInternalHandler = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message m) {
				switch (m.what) {
				case FLACRecorder.MSG_AMPLITUDES:
					FLACRecorder.Amplitudes amp = (FLACRecorder.Amplitudes) m.obj;

					// Create a copy of the amplitude in mLastAmplitudes; we'll
					// use
					// that when we restart recording to calculate the position
					// within the Boo.
					mLastAmplitudes = new FLACRecorder.Amplitudes(amp);

//					if (null != mAmplitudes) {
//						amp.mPosition += mAmplitudes.mPosition;
//					}
					mUpchainHandler.obtainMessage(FLACRecorder.MSG_AMPLITUDES,
							amp).sendToTarget();
					return true;

				case MSG_END_OF_RECORDING:
					// Update stats - at this point, mLastAmp should really be
					// the last set
					// of amplitudes we got from the recorder.
					
//					if (null == mAmplitudes) {
//						mAmplitudes = mLastAmplitudes;
//					} else {
//						mAmplitudes.accumulate(mLastAmplitudes);
//					}

					if (null != mRecording && null != mLastAmplitudes) {
						mRecording.mDuration = mLastAmplitudes.mPosition / 1000.0;
						mRecording = null;
					}

					mUpchainHandler.obtainMessage(MSG_END_OF_RECORDING)
							.sendToTarget();
					return true;

				default:
					mUpchainHandler.obtainMessage(m.what, m.obj).sendToTarget();
					return true;
				}
			}
		});
		
		mDir = getPath() + File.separator;
		mListRecordingIndex = 0;
		mRecordingFile = new LinkedList<String>();
		mDeviceAudioRecordSettingValue = getDeviceAudioRecordSettingValue();
	}

	public void start() {
		// Every time we start recording, we create a new recorder instance, and
		// record to a new file.
		// That means if there's still a recorder instance running (shouldn't
		// happen!), we'll kill it.

		if (null != mRecorder) {
			stop();
		}
		
		if(mDeviceAudioRecordSettingValue==null){
			Log.e(LTAG, "이 기기는 오디오레코드를 지원하지 않습니다!"); // 나중에 메시지처리해서 토스트로 보여주기
			return;
		}

		// Add a new recording to the Boo.
		mRecording = getRecording();
		mRecordingFile.offer(mRecording.mFilename);

		// Start recording!
		mRecorder = new FLACRecorder(mDeviceAudioRecordSettingValue, mRecording.mFilename, mInternalHandler);
		mRecorder.start();
		mRecorder.startRecording();
	}

	public void stop() {
		if (null == mRecorder) {
			// We're done.
			return;
		}

		// Pause recording & kill recorder
		mRecorder.stopRecording();
		try {
			mRecorder.join();
		} catch (InterruptedException ex) {
			// pass
		}
		mRecorder = null;
		
		// 최근 저장한 파일이름을 변수에 저장해둔다.
		//mLatelyFile = mRecording.mFilename;

		// Post an end-of-recording message; that'll update stats.
		mInternalHandler.obtainMessage(MSG_END_OF_RECORDING).sendToTarget();
	}

	public boolean isRecording() {
		if (null == mRecorder) {
			return false;
		}
		return mRecorder.isRecording();
	}

	public double getDuration() {
		if (null == mAmplitudes) {
			return 0f;
		}
		return mAmplitudes.mPosition / 1000.0;
	}

	public FLACRecorder.Amplitudes getAmplitudes() {
		return mAmplitudes;
	}
	
	public String getFilename(){
		//return mLatelyFile;
		return mRecordingFile.poll();
	}

	// custom function
	private Recording getRecording() {
		if (mListRecording == null) {
			mListRecording = new LinkedList<Recording>();
		}

		Recording rec = null;
		if (mListRecording.size() == TEMPFILENUMBER) {
			Recording r = mListRecording.get(mListRecordingIndex);
			rec = r;
		}

		if (null == rec) {
			rec = new Recording(mDir + mListRecordingIndex + RECORDING_EXTENSION);
			mListRecording.add(rec);
		}
		
		mListRecordingIndex++;
		if (mListRecordingIndex >= TEMPFILENUMBER)
			mListRecordingIndex = 0;

		return rec;
	}
	
	private ArrayList<Integer> getDeviceAudioRecordSettingValue(){
		ArrayList <Integer> arrayList = new ArrayList<Integer>(4);
		
		final int sample_rates[] = { 96000, 44100, 22050, 11025, 8000 };
		final int configs[] = { AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.CHANNEL_CONFIGURATION_STEREO };
		final int formats[] = { AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT };
		
		int sample_rate = -1;
		int channel_config = -1;
		int format = -1;
		
		int bufsize = AudioRecord.ERROR_BAD_VALUE;
		AudioRecord recorder = null;
		
		boolean found = false;
		for (int x = 0; !found && x < formats.length; ++x) {
			format = formats[x];

			for (int y = 0; !found && y < sample_rates.length; ++y) {
				sample_rate = sample_rates[y];

				for (int z = 0; !found && z < configs.length; ++z) {
					channel_config = configs[z];

					Log.d(LTAG, "Trying: " + format + "/" + channel_config
							+ "/" + sample_rate);
					bufsize = 2 * AudioRecord.getMinBufferSize(sample_rate,
							channel_config, format);
					Log.d(LTAG, "Bufsize: " + bufsize);

					// Handle invalid configs
					if (AudioRecord.ERROR_BAD_VALUE == bufsize) {
						continue;
					}
					if (AudioRecord.ERROR == bufsize) {
						Log.e(LTAG, "Unable to query hardware!");
						return null;
					}

					try {
						// Set up recorder
						recorder = new AudioRecord(
								MediaRecorder.AudioSource.MIC, sample_rate,
								channel_config, format, bufsize);
						int istate = recorder.getState();
						if (istate != AudioRecord.STATE_INITIALIZED) 
							continue;
					} catch (IllegalArgumentException ex) {
						recorder = null;
						Log.e(LTAG, "Failed to set up recorder!");
						continue;
					}

					found = true;
					break;
				}
			}
		}

		if (!found) {
			Log.e(LTAG, "Sample rate, channel config or format not supported!");
			return null;
		}
		
		recorder.release();
		
		arrayList.add(sample_rate);
		arrayList.add(channel_config);
		arrayList.add(format);
		arrayList.add(bufsize);
		
		return arrayList;
	}

	private String getPath() {
		Context ctx = mContext.get();
		if (null == ctx) {
			return null;
		}

		List<String> paths = new LinkedList<String>();

		Log.d(LTAG, "BooManager looking for exernal storage.");
		// Data on SD card; first preferred path.
		String base;
		try {
			base = Environment.getExternalStorageDirectory().getPath();
			Log.d(LTAG, "Got:  " + base);
			File d = new File(base);
			d.listFiles(); // see if we can actually access what's given to us.
							// If not then fall back to internal storage
			if (!d.canWrite())
				throw new IOException();
		} catch (Exception ex) {
			Log.d(LTAG,
					"Failed to read back form external sdcard.  Falling back to internal storage.  Caught exception "
							+ ex.getMessage());
			base = ctx.getCacheDir().getPath();
		}

		base += File.separator + "data" + File.separator + ctx.getPackageName()
				+ File.separator + "rec";
		paths.add(base);

		// Private data, for compatibility with old versions and fallbacks.
		base = ctx.getDir(DATA_DIR_PREFIX, Context.MODE_PRIVATE).getPath();
		paths.add(base);

		//
		// First determine what the preferred directory is for creating Boos.
		String path = paths.get(0);

		// Make sure the create path exists and is a directory.
		File d = new File(path);
		if (!d.exists()) {
			d.mkdirs();
		}
		if (!d.isDirectory() || !d.canWrite()) {
			throw new IllegalStateException(
					"Create path '"
							+ path
							+ "' either does not exist, or exists but is not a directory.");
		}
		
		return path;
	}
}
