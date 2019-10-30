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

# Algorithms
Algorithms can be implemented in one of four development environments -- Android, Python, React and React-Native. Each modality is summarized in the following table.

| Modality  | Resides on  | Initiated by  | Language | Compilation steps   |
|---|---|---|---|---|
| Android  | Phone  | Wakes up  |  Java |  `gradle build` |
| React Native  |  Phone | Wakes up  | Javascript  | `react-native build`  |
|  React | Browser  | User initiated  | Javascript  |  `react build` |
| Python scripts | Server  | Wakes up  |  Python | Copy over scripts  |

They are stored in the `algorithms` folder in the repository. Each modality has a sub-section which 
