# Overview

`CarLab` is a data collection testbed to facilitate research involving vehicular data collection. It consists of an **Android library**, a web-based **trip visualization dashboard** and a web-based **experiment creation/dissemination/management interface**.

### Android Library

The Android library has many useful components and a Service which handles data collection needs.  The capabilities of the library include: 

1. Interfacing with phone sensors, OBD dongles, OpenXC dongle, and web-based sensors. This support includes setting up and destroying access to these resources.
2. Storage and upload of the data from the background. CarLab stores all collected trip data in the local Android file system and uploads them asynchronously when it can connect to the Internet through a WiFi connection. 
3. Triggering data collection based on a developer-supplied triggering condition. For example, CarLab data collection can be triggered through proximity to OBD dongle, proximity to OpenXC dongle, and "in-vehicle" state estimated through IMU/GPS sensors. Example code for these triggers are included in the repository. 



Read more about developing and using CarLab Android apps [here](Android Library.html).

Read instructions for drivers installing the Android app [here](Participating.html).

Read more about the web-based experiment-management interface [here](Experiment Creation and Management.html).