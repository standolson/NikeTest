package com.ppp.niketest;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements SensorEventListener
{

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final long REALTIME_REPORTING_INTERVAL_MS = 100;
	private static final long NETWORK_REPORTING_INTERVAL_MS = 1000;
	private static final long NETWORK_PUSH_INTERVAL_MS = 10000;
	private static final float MINIMUM_ACCELERATION = 0.2f;

	// The current and total velocities are represented on the gauge
	// at the right most position.  Should the user exceed either of
	// these at runtime, we bump them by a factor of 2.  We keep the
	// defaults as a reset from the overflow menu will reset the maximums
	// to their defaults.
	private static final float DEFAULT_MAXIMUM_CURRENT_VELOCITY = 5f;
	private static float MAXIMUM_CURRENT_VELOCITY = DEFAULT_MAXIMUM_CURRENT_VELOCITY;
	private static final float DEFAULT_MAXIMUM_TOTAL_VELOCITY = 1000f;
	private static float MAXIMUM_TOTAL_VELOCITY = DEFAULT_MAXIMUM_TOTAL_VELOCITY;

	private final int accelerometerDelay = SensorManager.SENSOR_DELAY_UI;
	private final int gyroscopeDelay = SensorManager.SENSOR_DELAY_UI;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor gyroscope;

	private long lastAcceleratorTime;
	private long lastGyroscopeTime;
	private float maxVelocity = 0f;
	private float totalVelocity = 0f;

	private ArrayList<NetworkData> dataToPush;
	private long lastPushTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// Make sure we have an accelerometer and a gyroscope
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if ((accelerometer == null) || (gyroscope == null))  {
			Log.e(TAG, "must have accelerometer and gyroscope");
			// TODO: Dialog that finishes activity on dismiss
		}

		if (savedInstanceState != null) {
			maxVelocity = savedInstanceState.getFloat("maxVelocity", 0f);
			totalVelocity = savedInstanceState.getFloat("totalVelocity", 0f);
			dataToPush = savedInstanceState.getParcelableArrayList("dataToPush");
			lastPushTime = savedInstanceState.getLong("lastPushTime");
		}
		else {
			dataToPush = new ArrayList<NetworkData>();
			lastPushTime = System.currentTimeMillis();
		}

    }

	@Override
	public void onStart()
	{

		super.onStart();

		// Register the sensors on startup
		if (accelerometer != null) {
			sensorManager.registerListener(this, accelerometer, accelerometerDelay);
			lastAcceleratorTime = System.currentTimeMillis();
		}
		if (gyroscope != null) {
			sensorManager.registerListener(this, gyroscope, gyroscopeDelay);
			lastGyroscopeTime = System.currentTimeMillis();
		}

	}

	@Override
	public void onStop()
	{

		super.onStop();

		// Stopping the app so unregister both sensors
		if (accelerometer != null)
			sensorManager.unregisterListener(this, accelerometer);
		if (gyroscope != null)
			sensorManager.unregisterListener(this, gyroscope);

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putFloat("maxVelocity", maxVelocity);
		savedInstanceState.putFloat("totalVelocity", totalVelocity);
		savedInstanceState.putParcelableArrayList("dataToPush", dataToPush);
		savedInstanceState.putLong("lastPushTime", lastPushTime);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

		// Let the user see any incoming messages pushed from other
		// devices
        if (id == R.id.show_messages)  {
			Intent intent = new Intent(this, ShowMessagesActivity.class);
			startActivity(intent);
			return true;
		}

		// Reset our state
		else if (id == R.id.reset)  {
			totalVelocity = 0f;
			maxVelocity = 0f;
			dataToPush = new ArrayList<NetworkData>();
			lastPushTime = System.currentTimeMillis();
			MAXIMUM_TOTAL_VELOCITY = DEFAULT_MAXIMUM_TOTAL_VELOCITY;
			MAXIMUM_CURRENT_VELOCITY = DEFAULT_MAXIMUM_CURRENT_VELOCITY;
			return true;
		}

        return super.onOptionsItemSelected(item);

    }

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor == accelerometer)
			onAccelerometerChanged(event);
		else if (event.sensor == gyroscope)
			onGyroscopeChanged(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		if (sensor == accelerometer)
			onAccelerometerAccuracyChanged(accuracy);
		else if (sensor == gyroscope)
			onGyroscopeAccuracyChanged(accuracy);
	}

	private void onAccelerometerChanged(SensorEvent event)
	{

		// Report sensor values only if we have met the minimum
		// amount of time between reporting intervals
		long curTime = System.currentTimeMillis();
		if (curTime - lastAcceleratorTime > REALTIME_REPORTING_INTERVAL_MS)  {

			long diffTime = curTime - lastAcceleratorTime;
			lastAcceleratorTime = curTime;

			setFloatTextView(R.id.xAcceleration, R.string.x_acceleration, event.values[0]);
			setFloatTextView(R.id.yAcceleration, R.string.y_acceleration, event.values[1]);
			setFloatTextView(R.id.zAcceleration, R.string.z_acceleration, event.values[2]);

			// Get the current velocity and update the maximum and total
			// velocities
			float currentVelocity = computeVelocity(event.values, diffTime);
			if (currentVelocity > maxVelocity)
				maxVelocity = currentVelocity;
			totalVelocity += currentVelocity;

			// Update the maximum velocity the gauge shows if we exceed
			// the current maximum
			if (currentVelocity > MAXIMUM_CURRENT_VELOCITY)
				MAXIMUM_CURRENT_VELOCITY *= 2;

			// Update the maximum total velocity the gauge shows if we
			// exceeded the current maximum
			if (totalVelocity > MAXIMUM_TOTAL_VELOCITY)
				MAXIMUM_TOTAL_VELOCITY *= 2;

			setFloatTextView(R.id.dialVelocity, R.string.dial_velocity, currentVelocity);
			setFloatTextView(R.id.currentVelocity, R.string.current_velocity, currentVelocity);
			setFloatTextView(R.id.totalVelocity, R.string.total_velocity, totalVelocity);
			setFloatTextView(R.id.maxVelocity, R.string.max_velocity, maxVelocity);

			drawNeedle(currentVelocity, MAXIMUM_CURRENT_VELOCITY, R.id.currentVelocityNeedleView);
			drawNeedle(totalVelocity, MAXIMUM_TOTAL_VELOCITY, R.id.totalVelocityNeedleView);

			pushData(curTime, event.values, currentVelocity, maxVelocity);

		}

	}

	private void onAccelerometerAccuracyChanged(int accuracy) {}

	private void onGyroscopeChanged(SensorEvent event)
	{

		// Report sensor values only if we have met the minimum
		// amount of time between reporting intervals
		long curTime = System.currentTimeMillis();
		if (curTime - lastGyroscopeTime > REALTIME_REPORTING_INTERVAL_MS)  {
			lastGyroscopeTime = curTime;
			setFloatTextView(R.id.azimuth, R.string.azimuth, event.values[0]);
			setFloatTextView(R.id.pitch, R.string.pitch, event.values[1]);
			setFloatTextView(R.id.roll, R.string.roll, event.values[2]);
		}

	}

	private void onGyroscopeAccuracyChanged(int accuracy) {}

	private void setFloatTextView(int id, int stringId, float value)
	{
		((TextView) findViewById(id)).setText(getString(stringId, value));
	}

	private float computeVelocity(float[] values, long diffTime)
	{

		float time = (float) diffTime / 1000f;
		float newVelocity = 0.0f;
		float currentVelocity = 0.0f;

		// Compute the velocity over the given period of time
		// for all axis and accumulate the absolute velocity of
		// all axis.
		// We're filtering acceleration here to remove noise
		// from certain devices' sensors.
		for (int i = 0; i < 3; i += 1)  {
			if (Math.abs(values[i]) < MINIMUM_ACCELERATION)
				values[i] = 0;
			newVelocity = values[i] * time;
			currentVelocity += Math.abs(newVelocity);
		}

		return currentVelocity;

	}

	private void drawNeedle(float velocity, float maximumVelocity, int viewId)
	{

		ImageView iv = (ImageView) findViewById(viewId);

		// The left most needle position is zero.  The right most
		// position is the maximum velocity given.  Compute the angle
		// of rotation for our needle given that its full span is
		// 270 degrees.
		float pct = velocity / maximumVelocity;

		// Move the needle to the center and rotate it around its
		// origin by the appropriate percentage of the measurable
		// area offset by the bias to the zero position on the dial.
		//
		// Center of the dial is (100dip, 100dip) since the image
		// is (200dip, 200dip).  We're just lucky it worked out that
		// way.
		iv.setX(dipToPixels(100));
		iv.setY(dipToPixels(100));
		iv.setPivotX(0);
		iv.setPivotY(0);
		iv.setRotation(-225 + (270 * pct));

	}

	private float dipToPixels(int dip)
	{
		float density = getResources().getDisplayMetrics().density;
		return dip * density + .5f;
	}

	private void pushData(long timestamp, float[] values, float currentVelocity, float maxVelocity)
	{

		// If we haven't stored any data to the pushable data set in
		// the last second, add this data to the data set.
		//
		// Degenerate case is that we haven't saved any data yet.
		if (dataToPush.size() == 0)  {
			NetworkData newData = new NetworkData(timestamp, values, currentVelocity, maxVelocity);
			dataToPush.add(newData);
			return;
		}
		else {

			// We've already put some data into the pushable data set.  Get the
			// last element pushed and use its timestamp to determine if we should
			// add this latest data.
			NetworkData oldest = dataToPush.get(dataToPush.size() - 1);
			if (timestamp - oldest.timestamp > NETWORK_REPORTING_INTERVAL_MS)  {
				NetworkData newData = new NetworkData(timestamp, values, currentVelocity, maxVelocity);
				dataToPush.add(newData);
			}

		}

		// We dump data to the network every 60 seconds.  See if this
		// latest data meets that criteria.
		if (timestamp - lastPushTime > NETWORK_PUSH_INTERVAL_MS)  {
			// TODO: dump data
			Log.e(TAG, "pushing data at " + timestamp);
			Toast.makeText(this, getString(R.string.push_data_info_msg), Toast.LENGTH_LONG).show();
			NetworkConnection.getInstance().pushData(dataToPush, new PublishCallback());
			lastPushTime = timestamp;
			dataToPush = new ArrayList<NetworkData>();
			return;
		}

	}

}
