package com.ppp.niketest;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

public class PublishCallback extends Callback {

	private static final String TAG = PublishCallback.class.getSimpleName();

	@Override
	public void successCallback(String channel, Object message)
	{
		Log.i(TAG, "successCallback: channel '" + channel + "' message " + message);
	}

	@Override
	public void errorCallback(String channel, PubnubError error)
	{
		Log.e(TAG, "errorCallback: channel '" + channel + "' error '" + error.getErrorString() + "'");
	}

}
