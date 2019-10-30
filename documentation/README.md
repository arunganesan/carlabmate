`CarLab` is an on-demand data collection builder for vehicular research. Using a high-level specification of the data collection requirements, it creates a custom-build data collection platform, which includes all the tools necessary to carry out the data collection campaign. 

There are three main ways to interact with `CarLab`. **Developers** can contribute algorithms which power the core of `CarLab`. An algorithm takes information as input and outputs information. A **data collection campaign designer** can use `CarLab` to craft a data collection campaign. The designer inputs a data collection specification into `CarLab` and iteratively refines it until it meets the data collection requirements. A **participant** who participates in the data collection signs up through `CarLab` web interface and installs the data collection tools.

# Table of contents
* Overview
  * Purpose of `CarLab`
  * Data collection campaign examples (a visual example of the entire process)
* Data collection designer
  * Data collection requirement specification
  * Refining the data collection strategy
* Experiment participant
  * Signing up for an experiment
  * Installing different components
* Algorithm developer
  * Creating a template algorithm
  * Local testing for each library
* Reference documentation
  * Libraries for each language
  * Available algorithms

# Data collection designer
The design phase is divided into two steps. The first step the designer inputs the high-level requirements. These requirements are the list of information they wish to collect, and the available devices, and any blacklisted information (e.g. location) or sensors (e.g. GPS). Using this, `CarLab` searches through the library of available algorithms and automatically finds a suitable data collection plan. 
In the second step, the designer revises this plan by editing any individual data collection modules or by overwriting any of the strategy. After the data collection is designed, it outputs the following strategy JSON file. This is finally used to build the data collection platform.

```json
{
  "algorithms": [
    { "algorithm": "watchfon", "information": "car-steering", "save": true },
    { "algorithm": "watchfon", "information": "car-speed" },
    { "algorithm": "aligned-imu", "information": "world-aligned-gyro" },
    { "algorithm": "aligned-imu", "information": "world-aligned-accel" },
    { "algorithm": "aligned-imu", "information": "rotation" },
    { "algorithm": "android-raw-sensors", "information": "gravity"},
    { "algorithm": "android-raw-sensors", "information": "magnetometer"},
    { "algorithm": "android-raw-sensors", "information": "gyro"},
    { "algorithm": "android-raw-sensors", "information": "accel"},
    { "algorithm": "android-raw-sensors", "information": "location"}
  ],
  "wiring": [
    { "algorithm": 0, "inputs": [1, 3, 2] },
    { "algorithm": 1, "inputs": [10, 4] },
    { "algorithm": 3, "inputs": [8, 5] },
    { "algorithm": 4, "inputs": [9, 5] },
    { "algorithm": 5, "inputs": [6, 7] }
  ]
}
```

# Algorithm developer
Algorithms are implemented as standalone libraries under each of the supported languages. The entry point into each algorithm is a class with a set of callback functions to generate each output information. This function is invoked with each of the required information as parameters. The class is created and sustained during the data collection so it can keep any internal state. 

The overall process of generating and filling in the algorithm is shown in the following figure. 
![algorithm-generation](images/algorithm-generation.png)

The entry point into an algorithm is the algorithm spec JSON file. This file gives the main details of the algorithm. It specifies which information is produced by this algorithm and their corresponding input. 

```JSON
[{
    "module": "aligned-imu",
    "classname": "AlignedIMU",
    "functions": [
        { 
            "output": "rotation", 
            "input": ["gravity", "magnetometer"] 
        },{ 
            "output": "world-aligned-gyro", 
            "input": ["gyro", "rotation"] 
        },{ 
            "output": "world-aligned-accel", 
            "input": ["accel", "rotation"] 
        }
    ]
}]
```

This information is used in the algorithm-generation script. The script is invoked using `gen-algorithm android spec.json` command. This creates the function stubs which are invoked during run-time. The developer can then simply fill in the stub. The generation script also creates a sandbox wrapper script which imports the algorithm library. The wrapper also provides support for feeding in dummy data for any of the input/output of the algorithm allowing rapid testing.

```java
package edu.umich.aligned_imu;
import android.content.Context;
import edu.umich.carlab.CLDataProvider;

public class AlignedIMU extends Algorithm {
    public AlignedIMU(CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public float[][] produceRotation (Float [] m, Float [] g) {}

    @Override
    public Float[] produceAlignedGyro (Float [] gyro, float [][] rm) {}

    @Override
    public Float[] produceAlignedAccel (Float [] accel, float [] [] rm) {}
}
```

# Reference documentation
Algorithms can be implemented in one of four development environments -- Android, Python, React and React-Native. Each modality is summarized in the following table.

| Modality  | Resides on  | Initiated by  | Language | Compilation steps   |
|---|---|---|---|---|
| Android  | Phone  | Wakes up  |  Java |  `gradle build` |
| React Native  |  Phone | Wakes up  | Javascript  | `react-native build`  |
|  React | Browser  | User initiated  | Javascript  |  `react build` |
| Python scripts | Server  | Wakes up  |  Python | Copy over scripts  |

They are stored in the `algorithms` folder in the repository. Each modality has a sub-section which 
