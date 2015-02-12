package com.ppp.niketest;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONArray;

import java.util.ArrayList;

public class NetworkConnection {

	private static final String TAG = NetworkConnection.class.getSimpleName();

	public static final String PUBLISH_KEY = "pub-c-3ea5710c-a293-4ade-b5d7-ba51266a2072";
	public static final String SUBSCRIBE_KEY = "sub-c-0e94f2fc-b1a3-11e4-b35d-02ee2ddab7fe";
	public static final String CHANNEL_NAME = "NikeTest";

	private static NetworkConnection instance;

	private Pubnub pubnub;

	private NetworkConnection()
	{
		pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);
	}

	public static NetworkConnection getInstance()
	{
		if ((instance == null) || (instance.pubnub == null))
			instance = new NetworkConnection();
		return instance;
	}

	public void pushData(ArrayList<NetworkData> data, Callback callback)
	{

		// If not initialized, do nothing
		if (pubnub == null)  {
			Log.e(TAG, "uninitiialized: unable to push data at this time");
			return;
		}

		// Create the JSON array that we'll be sending
		JSONArray array = new JSONArray();
		for (NetworkData d : data)
			array.put(d.toJSON());

		// Publish the data
		pubnub.publish(CHANNEL_NAME, array, callback);

	}

}
