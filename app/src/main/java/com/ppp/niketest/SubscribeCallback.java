package com.ppp.niketest;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

public class SubscribeCallback extends Callback {

	private static final String TAG = SubscribeCallback.class.getSimpleName();

	@Override
	public void connectCallback(String channel, Object message)
	{
		Log.i(TAG, "connectCallback: channel '" + channel + "' message '" + message + "'");
	}

	@Override
	public void disconnectCallback(String channel, Object message)
	{
		Log.w(TAG, "disconnectCallback: channel '" + channel + "' message '" + message + "'");
	}

	@Override
	public void reconnectCallback(String channel, Object message)
	{
		Log.w(TAG, "disconnectCallback: channel '" + channel + "' message '" + message + "'");
	}

	@Override
	public void successCallback(String channel, Object message)
	{
		Log.i(TAG, "disconnectCallback: channel '" + channel + "' message '" + message + "'");
	}

	@Override
	public void errorCallback(String channel, PubnubError error)
	{
		Log.e(TAG, "errorCallback: channel '" + channel + "' error '" + error.getErrorString() + "'");
	}

}
