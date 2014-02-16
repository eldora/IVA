package com.iva.record;

import java.io.Serializable;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class Recording implements Parcelable, Serializable {
	public String mFilename;
	public double mDuration;

	public Recording(String filename, double duration) {
		mFilename = filename;
		mDuration = duration;
	}

	public Recording(String filename) {
		mFilename = filename;
	}

	public String toString() {
		return String.format(Locale.US, "<%f:%s>", mDuration, mFilename);
	}

	/***************************************************************************
	 * Parcelable implementation
	 **/
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mFilename);
		out.writeDouble(mDuration);
	}

	public static final Parcelable.Creator<Recording> CREATOR = new Parcelable.Creator<Recording>() {
		public Recording createFromParcel(Parcel in) {
			return new Recording(in);
		}

		public Recording[] newArray(int size) {
			return new Recording[size];
		}
	};

	private Recording(Parcel in) {
		mFilename = in.readString();
		mDuration = in.readDouble();
	}
}
