package com.ppp.niketest;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class NetworkData implements Parcelable {

	private static final String TAG = NetworkData.class.getSimpleName();

	public long timestamp;
	public float[] accelerometerData;
	public float currentVelocity;
	public float maxVelocity;

	public NetworkData(long timestamp, float[] accelerometerData, float currentVelocity, float maxVelocity)
	{
		this.timestamp = timestamp;
		this.accelerometerData = accelerometerData;
		this.currentVelocity = currentVelocity;
		this.maxVelocity = maxVelocity;
	}

	public JSONObject toJSON()
	{

		JSONObject retval = new JSONObject();

		try {
			retval.put("timestamp", timestamp);
			JSONArray array = new JSONArray();
			for (int i = 0; i < accelerometerData.length; i += 1)
				array.put(i, accelerometerData[i]);
			retval.put("accelerometerData", array);
			retval.put("currentVelocity", currentVelocity);
			retval.put("maxVelocity", maxVelocity);
		}
		catch (Exception e) {
			Log.e(TAG, "unable to create JSONObject: " + e);
			return null;
		}

		return retval;

	}

	public static final Parcelable.Creator<NetworkData> CREATOR = new Parcelable.Creator<NetworkData>()
	{
		@Override
		public NetworkData createFromParcel(Parcel in)
		{
			return new NetworkData(in);
		}
		@Override
		public NetworkData[] newArray(int size)
		{
			return new NetworkData[size];
		}
	};

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeLong(timestamp);
		dest.writeInt(accelerometerData.length);
		for (int i = 0; i < accelerometerData.length; i += 1)
			dest.writeFloat(accelerometerData[i]);
		dest.writeFloat(currentVelocity);
		dest.writeFloat(maxVelocity);
	}

	public NetworkData(Parcel in)
	{
		timestamp = in.readLong();
		int size = in.readInt();
		accelerometerData = new float[size];
		for (int i = 0; i < size; i += 1)
			accelerometerData[i] = in.readFloat();
		currentVelocity = in.readFloat();
		maxVelocity = in.readFloat();
	}

}
