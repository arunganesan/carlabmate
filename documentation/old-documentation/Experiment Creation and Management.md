# Experiment creation and management workflow

All CarLab experiments must have the same name as the Android module. The experiment creation and management workflow primarily serves to maintain different version of the data collection app, and custom-compile the APK for each user. The custom compilation overwrites the `res/values/values.xml` file for each user. See [Android Library](Android Library.html) for more info.

The experiment management page can be found by clicking on "Researcher View" on the home pag or directly visiting `http://barca.eecs.umich.edu:9000/experiment/myexperiments`.

![](images/homepage.png)



The home page lists each experiment. To create a new experiment that matches the Android app module, please enter the exact same name of the module and click on "Create new experiment". If this succeeds, the new experiment will appear in the list of experiments below.



![image-20180831114208769](images/creation01.png)

Once created, click to open the experiment. There are four main ways to interact with each experiment -- **Trips** (to see the trips collected for this experiment across all participants), **Compiler** (to generate new versions of the experiment from the latest repository commits or switch to an earlier version), **Logs** (to see CLogs of users who participate in this experiment -- see [Android Library](Android Library.html) for more information about CLogs) and **Dashboard**  (to track the growth of this experiment campaign).



![image-20180831114411655](images/creation02.png)

#### Trips

![image-20180831114528755](images/creation-trips.png)

This page lists all the trips collected for this dataset. You can click and select multiple trips, or select all of them at once by clicking "Select All". After selecting trips, you can click "Open Selected" to open them in the dashboard (see the last portion of [Participating](Participating.html)) or "Download Selected" to download the CSV files of each trip. 



#### Compiler

![image-20180831114901305](images/compile03.png)

Press "Compile New Version" to automatically do a `git pull` from the repository and associate the HEAD commit hash ID to a new version number. *If HEAD is already associated with a version number, this won't update the version number*. After a new version is created, a new entry will appear in the table. Click on the version to switch to that version. In the backend, when a "switch" happens, it goes through all users signed up for this experiment and recompiles individualized APK for each of them. This process takes a while. The page will show a progress indicator. Once finished, it will highlight the current deployed version in green. *You can switch to earlier versions*. When the version number changes, it will prompt users to download and install new APK.



#### Logs

![image-20180831115300807](images/logs.png)

This shows 100 entries of the CLogs collected. This is a very crude way to reaad the CLog data. It has several serious limitations including:

* It just passes in the raw WHERE clause of the SQL query. This is a terrible design, so please don't shoot yourself in the foot
* The logs are not tagged by the experiment, so it just shows all data collected for this UID. 
* It only shows 100 entries with no way to customize it now (except through the WHERE clause)





#### Dashboard

![image-20180831115529078](images/dashboard-experiment.png)

The dashboard page shows some interesting trackers for this experiment. It shows the number of users, the number of trips, and the number of hours of data. *Note: there is a bug with the way D3 is loaded through Rails. If the charts don't show up, just reload the page*
