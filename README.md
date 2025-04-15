Jack Weinstein jrweinstein@wpi.edu
Edward Stump ejstump@wpi.edu
Lily Bromberger lkbromberger@wpi.edu

# Step Counter App

## Overview
The Step Counter App is an Android application designed to track and visualize a user's daily step count. Built using Kotlin and Android Studio, the app leverages the device's accelerometer sensor to detect steps, stores the data in a local SQLite database, and displays it in a user-friendly interface. Key features include a live step counter, a daily step chart, swipe navigation to view past days, and orientation-specific layouts for better usability.

## Features and Implementation

### 1. Live Step Counter
- **Description**: Displays the current step count in real-time, controlled by Start, Stop, and Reset buttons.
- **Implementation**:
    - The `CounterViewModel` tracks the step count using `LiveData` (`stepCount`).
    - In `MainActivity.kt`, the `stepCount` is observed to update a `TextView` (`live_step_counter`) with the current count.
    - The Start button registers a sensor listener (`SensorManager`) to begin step detection using the accelerometer (`TYPE_LINEAR_ACCELERATION`).
    - The Stop button unregisters the listener to pause step counting.
    - The Reset button calls `CounterViewModel.resetSteps()` to set the count back to 0 and updates the UI accordingly.
    - Incremental steps are calculated by tracking the difference between consecutive step counts (`previousStepCount`) to ensure accurate database entries.

### 2. Daily Step Chart
- **Description**: Visualizes the user's steps for each hour of the day in a bar chart, with Y-axis ticks in increments of 1000 steps.
- **Implementation**:
    - A custom `View` class, `HourlyStepChart.kt`, is used to draw the chart.
    - The chart displays 24 bars (one per hour) using a `FloatArray` (`hourlySteps`) passed via `setHourlySteps`.
    - Bars are drawn using `Canvas.drawRect`, scaled to the maximum tick value (`tickMax`), which is calculated by rounding up the maximum hourly steps (`maxSteps`) to the nearest 1000 and adding 1000.
    - The Y-axis ticks are drawn in increments of 1000 up to `tickMax` using `Canvas.drawText`, ensuring the scale matches the bar heights.
    - X-axis labels show hours (0, 3, 6, ..., 21) for clarity.
    - The chart adapts to screen orientation with dynamic padding and text sizing based on the smaller dimension (width or height).

### 3. Swipe Navigation for Past Days
- **Description**: Users can swipe left or right on the chart to view step data for previous or next days (up to the current day).
- **Implementation**:
    - A `GestureDetector` in `MainActivity.kt` detects swipe gestures on the root layout.
    - Swiping left decrements the date by one day (`currentDate - dayMillis`), and swiping right increments it (`currentDate + dayMillis`), but prevents going past the current day.
    - The `StepViewModel` holds the current date in a `MutableLiveData` (`currentDate`), which is observed to update the UI (`updateUIForDate`).
    - A `TextView` (`swipe_instruction`) with a semi-transparent background guides users to swipe, positioned above the chart.

### 4. Orientation-Specific Layouts
- **Description**: The app adapts its layout based on the device's orientation for optimal usability.
- **Implementation**:
    - Two layouts are defined: `res/layout/activity_main.xml` (portrait) and `res/layout-land/activity_main.xml` (landscape).
    - **Portrait Layout**:
        - The chart spans the full width, with buttons (Start, Stop, Reset) in a horizontal row below it.
        - The live step counter is at the bottom.
    - **Landscape Layout**:
        - The chart is moved to the right (70% of the screen width), giving it more space.
        - Buttons are arranged in a vertical column on the left (30% of the screen width), along with the total steps, date, and live step counter.
    - `ConstraintLayout` with percentage-based constraints (`layout_constraintWidth_percent`) ensures proportional sizing in both orientations.

### 5. Persistent Storage with SQLite
- **Description**: Step data is stored persistently and retrieved to display daily totals and hourly breakdowns.
- **Implementation**:
    - A `StepDatabaseHelper` class (extending `SQLiteOpenHelper`) manages a local SQLite database with a single table (`step_data`) to store step entries (`id`, `timestamp`, `steps`).
    - The `insertStep` method inserts incremental step counts with timestamps.
    - `getStepsForDay` retrieves all step entries for a given day (between `startOfDay` and `endOfDay`).
    - `getTotalStepsForDay` calculates the total steps for a day using a SQL `SUM` query.
    - In `MainActivity.kt`, `updateUIForDate` aggregates steps into an hourly array (`hourlySteps`) by summing steps for each hour, which is then passed to the chart.

### 6. Dummy Data for Testing
- **Description**: Pre-populates the database with realistic step data for testing purposes.
- **Implementation**:
    - The `insertDummyData` function in `MainActivity.kt` generates data for 6 days (5 days before today, up to today).
    - Each day has hourly step counts: 300–1000 steps per hour during active hours (8 AM–8 PM), and 0–100 steps during inactive hours, resulting in daily totals of ~5000–10,000 steps.
    - A `SharedPreferences` flag (`isDummyDataInserted`) ensures dummy data is inserted only once when the app is first installed.
    - The database is cleared before inserting dummy data to prevent duplicate entries.

### 7. Step Counting Algorithm
- **Description**: Detects steps using the device's accelerometer sensor.
- **Implementation**:
    - [To be updated later]

## Project Structure
- `MainActivity.kt`: The main entry point, handling UI updates, sensor interactions, database operations, and swipe navigation.
- `HourlyStepChart.kt`: A custom `View` for rendering the daily step chart.
- `StepDatabaseHelper.kt`: Manages the SQLite database for storing and retrieving step data.
- `CounterViewModel.kt`: Manages the live step count using `LiveData`.
- `StepViewModel.kt`: Manages the current date for navigation.
- `res/layout/activity_main.xml`: Portrait layout.
- `res/layout-land/activity_main.xml`: Landscape layout.

## Getting Started
1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator (minimum SDK 24).
4. Use the Start button to begin step counting, swipe on the chart to view past days, and rotate the device to see the orientation-specific layouts.

## Dependencies
- AndroidX libraries (Core, AppCompat, ConstraintLayout, Lifecycle)
- Kotlin Coroutines
- JDSP library for signal processing (used in step detection)