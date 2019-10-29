# Participating in CarLab Data Collection

To participate in a CarLab-run experiment, users have to sign up for the experiment through the web interface and their Google account. Then they have to download the APK and install the app locally. They can also view their own data.



## Signing up

CarLab compiles a separate APK for each user. To participate in the data collection, users have to download the APK from a URL specially designed for that app. For example, to participate in our example module, users have to visit: 

`http://barca.eecs.umich.edu:9000/experiment/participate?shortname=example`

![image-20180831112641068](images/signup.png)

Once they sign up for the experiment, this page changes to the following:

![image-20180831112750203](images/checklater.png)

Once the APK has been compiled (done through the experimenter interface), this changes to: 



![image-20180831112955292](images/downloadapk.png)

## Installing APK

Users have to open the download page (see above) on their phones and log in with the same account. Once they log in, they have to enable install from unknown sources, install and open the APK. The workflow differs from phone to phone. The following is from Google Pixel 2 running Android 9.

![](images/installation.png)

All new updates are also installed this way. The app will notify the user if a new update is available.





## Viewing your data

After the data collection campagin runs on their phone for some time, it automatically uploads the data to the CarLab servers. Users can view their data by clicking on "Driver View" on the CarLab home page or directly visiting `http://barca.eecs.umich.edu:9000/view?`



![image-20180831113543591](images/homepage.png)



Users can browse through their trips using the dashboard, which is included in the CarLab server:

![image-20180831113645647](images/dashboard.png)