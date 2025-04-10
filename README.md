Jack Weinstein jrweinstein@wpi.edu
Edward Stump ejstump@wpi.edu
Lily Bromberger lkbromberger@wpi.edu

Part 2
Dual Sensor Support:
Added separate variables: linearAccelerometer and accelerometer to handle both sensor types
In initialize(), registered listeners for both Sensor.TYPE_LINEAR_ACCELERATION and Sensor.TYPE_ACCELEROMETER
Used sensorManager?.getDefaultSensor() to get instances of each sensor type

Separate File Writers:
Introduced two FileWriter variables: linearFileWriter and accelFileWriter
In initialize(), created two distinct CSV files using modified createCSVFile() with different prefixes
Wrote header lines ("Timestamp,X,Y,Z") to both files during initialization

File Creation with Prefixes:
Modified createCSVFile() to accept a prefix parameter ("LinearAccelerometerData" or "AccelerometerData")
Generated unique filenames with timestamp using the prefix: ${prefix}_$timestamp.csv
Saved files to Downloads directory using Environment.getExternalStoragePublicDirectory()

Data Recording and Separation:
In onSensorChanged(), used a when statement to check event.sensor.type
Directed linear acceleration data to linearFileWriter and regular acceleration data to accelFileWriter
Formatted and wrote timestamp and X,Y,Z values consistently for both sensor types

Recording Management:
Updated stopRecording() to close both file writers safely
Maintained existing recording state management with isRecording flag
Kept UI updates (button states and status text) synchronized with recording state

These implementations ensure that during a single recording session, both types of accelerometer data are captured simultaneously and saved to separate CSV files, maintaining the original app's functionality while meeting the new requirements.