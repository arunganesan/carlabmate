# Android Library

The Android library has many useful components and a Service which handles data collection needs.  The capabilities of the library include: 

1. Interfacing with phone sensors, OBD dongles, OpenXC dongle, and web-based sensors. This support includes setting up and destroying access to these resources.
2. Storage and upload of the data from the background. CarLab stores all collected trip data in the local Android file system and uploads them asynchronously when it can connect to the Internet through a WiFi connection. 
3. Triggering data collection based on a developer-supplied triggering condition. For example, CarLab data collection can be triggered through proximity to OBD dongle, proximity to OpenXC dongle, and "in-vehicle" state estimated through IMU/GPS sensors. Example code for these triggers are included in the repository.

#### Main Architectural Elements

The core functionality of CarLab is written in `CLService.java`. This launches a free-standing foreground service which is responsible for creating handles to all required data sources, receiving data, sending it to the requesting apps, and saving and uploading from files.

The `edu.umich.carlab.hal` package contains the code to interface with individual data sources. `edu.umich.carlab.sources` lists the sensors available through CarLab.



`edu.umich.carlab.clog` has a cloud-based logging service. This has a very similar interface to Android's `Log.v` function calls. Before writing content to the CLog, you must call `CLogDatabaseHelper.initializeIfNeeded(this);`with a valid "context" object. This is a static call which initializes the context used in CLog. The Clog data can be read on the server at the [Log page](http://barca.eecs.umich.edu:9000/experiment/logs?shortname=savad-oxc).



#### Developing for CarLab Android

To develop using CarLab, you must include the library in the Gradle file for each module, and fire an intent with the `MASTER_SWITCH_ON` action to CLService. Similarly, to turn off CarLab, you must fire the `MASTER_SWITCH_OFF` action in an intent directed towards CLService.

> For sample code, refer to the existing modules in the Android project. The examples provided below are for the "example" module (and the experiment wih the same name).



1. First visit [the website](http://barca.eecs.umich.edu:9000/experiment/myexperiments) to create a new experiment. This is an important first step because you will create a unique experiment name and experiment ID, which will be used later on.
2. Open `CarLabLite` in Android studio
3. Create a new "Phone & Table" module. *The module name should match the experiment name created in step 1*. Use min SDK 21 (Android 5.0) for the new module. Choose an empty activity as the starting. This module will contain a simple activity used to administer your experiment's apps. Wait for Android Studio to finish creating the module. Once created, it will show up in the project browser.
4.  Make these changes in the gradle.build library for the new module:
   * Set compileSdkVersion and targetSdkVersion to 26 (this is somehow related to the SDK version in the imported CarLab libraries)
   * Add the following exclusions to avoid conflicts with the Apache Commons libraries:

- ```groovy
  configurations {
      all {
          exclude module: 'httpclient'
          exclude module: 'commons-logging'
      }
  }
  ```

  - Remove this appcompat library: `implementation 'com.android.support:appcompat-v7:28.0.0-rc02'` and replace it with these for SDK 26: `implementation 'com.android.support:appcompat-v7:26.1.0'` and `implementation 'com.android.support:support-v4:26.1.0'` 
  - Import the CarLab library: `implementation project(":carlab")`
  - [Optional] If you want to re-use the carlab UI project, also include `implementation project(":carlabui")`. This project has some useful UI components such as a table to show the data in real time. See one of the existing modules (e.g. `debug`) for instructions on how to use this.
  - Sync the Gradle files so that the new changes take effect.

5. Add the following in the `res/values/values.xml` file. **This file is over-written when CarLab is compiled on the server for each user**. 
   Don't leave anything here you would want preserved. The values that you hardcode here are only used during testing. This file is automatically overwritten with the experiment participant's details when they install it from the website. In order to successfully upload the test data, you have to fill it in with your project shortname and project ID. You can find these details in the [web dashboard](http://barca.eecs.umich.edu:9000/experiment/myexperiments).

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="uid">111088096967389001979 OR YOUR OWN UID</string>
    <integer name="version">1</integer>
    <integer name="experimentID">YOUR PROJET ID</integer>
    <string name="shortname">WITH YOUR PROJECT NAME</string>
</resources>
```

------



That's all we need for setup. Next, we need to extend the `edu.umich.carlab.apps.App` class (or one of it's sub-classes) to specify what kind of data we want to collect. Please refer to the `edu.umich.example.ExampleApp` file in the `example` module for an example of this. `CLService` keeps track of the running apps and sends them the data requested in the App specification Java file.


Once you create the App, you have to add it to the list of apps that `CLService`  will start when it wakes up. You also have to add any other apps that they depend on. This can be done by saving the classname in Shared Preferences like this:



```java
Set<String> staticClassNames = new HashSet<>();
staticClassNames.add(ExampleApp.class.getCanonicalName());
staticClassNames.add(VehicleAlignment.class.getCanonicalName());

prefs
        .edit()
        .putStringSet(Static_Apps, staticClassNames)
    	.apply();
```

------

Finally, we need to start and stop CarLab and, optionally, display some useful information to the user.  Each `App` optionally generates a visualization. If you wish to show the visualization to the user, you have to bind to `CLService`, and acquire and load the visualization in the following way:

```java
app = carlabService.getRunningApp(appClassName);
View appView = app.initializeVisualization(mainActivity);
if (appView != null) visWrapper.addView(appView);
```

`MainActivity.java` in the example module starts `CLService` onResume() and shuts it down onPause(). Typically, it's more useful to start and stop `CLService` based on some trigger, so that it can run in the background. Examples of this can be found in `savad-obd` and `savad-phone` modules.



### TODO for the CarLab Android App

- The update feature and some useful UI are in the carlabui library module. We need better documentation on how to use these in general with new modules, instead of recreating it every time.
- The default values in the `values.xml` file might accidentally over-write data or enter bogus data in the database. We need a way to mark these as test. The real values for `values.xml` are written when the server compiles and deploys CarLab to each user.



