# NikeTest
Demonstration Android app for Nike

### Description

This app uses the Android SDK's sensor capabilities to access the accelerometer
and gyroscope of the device.  It then displays the values of those sensors every
1/10th of a second as text.

### Gauge

A crude velocity of the device is computed and displayed on a gauge.

A red pointer indicates the device's current velocity and a green pointer indicates
the device's total velocities over time.  The maximum velocity the device has
reached is also displayed in text.

The initial maximum values the gauge can display are a current velocity of 5
meters per second and a total velocity of 1000 meters per second.  Should either
of these maximums be exceeded, they are doubled and doubled again and again
should the new gauge maximums be exceeded.

### Data Publish and Subscribe

Every second the current accelerometer data and the current and maximum velocities
are captured and stored.  This continues for 10 seconds and then all of these
second-by-second readings are sent to PubNub's publish/subscribe system.

In order to prove that this data is being sent to PubNub, the "Show Messages"
option in the overflow menu will start monitoring PubNub for these messages.  To
test, run the app on two different devices; one capturing data and the other
showing the incoming data.

### Reset

To reset the app to its initial state (zero maximum and total velocities,
default maximum gauge values, and cleared publish data), use the "Reset" overflow
menu option.
