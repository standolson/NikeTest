package com.ppp.niketest;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

public class ShowMessagesActivity extends Activity {

	private ScrollView scroller;
	private LinearLayout content;
	private Pubnub pubnub;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_messages);

		scroller = (ScrollView) findViewById(R.id.scroll_view);
		content = (LinearLayout) findViewById(R.id.messages_container);

		newTextMessage("Monitoring channel '" + NetworkConnection.CHANNEL_NAME + "'");

		pubnub = new Pubnub(NetworkConnection.PUBLISH_KEY, NetworkConnection.SUBSCRIBE_KEY);

	}

	@Override
	public void onStart()
	{

		super.onStart();

		try {

			pubnub.subscribe(NetworkConnection.CHANNEL_NAME, new Callback() {

				@Override
				public void successCallback(String channel, final Object message)
				{
					runOnUiThread(new Runnable() {
						@Override
						public void run() { newTextMessage(message.toString()); }
					});
				}


				@Override
				public void errorCallback(String channel, PubnubError error)
				{
					newTextMessage("error: " + error.getErrorString());
				}

			});
		}
		catch (Exception e) {}

	}

	@Override
	public void onStop()
	{
		super.onStop();
		pubnub.unsubscribe(NetworkConnection.CHANNEL_NAME);
	}

	private void newTextMessage(String text)
	{

		TextView newMessage = new TextView(this);

		// Create the new message
		LinearLayout.LayoutParams params =
			new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		newMessage.setLayoutParams(params);
		newMessage.setText(text);
		newMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		newMessage.setTextColor(getResources().getColor(android.R.color.black));

		// Add the new message to the layout and cause the ScrollView to move
		// to the end (ala "tail -f")
		content.addView(newMessage);
		scroller.post(new Runnable() {
			@Override
			public void run() { scroller.fullScroll(View.FOCUS_DOWN); }
		});

	}

}
