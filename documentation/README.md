CarLab is a vehicular data collection builder. It has a library of algorithms maintained in the repository, which is contributed by multiple developers. Each algorithm defines the set of information it requires, and outputs a set of information. Using these algorithms, CarLab has a bi-partite graph of information and algorithms which are used to create the information. For each data collection requirement, CarLab spins up a new data collection platform copying the necessary algorithms to satisfy the data collection requirements.

# Algorithms

Algorithms can be implemented in one of four development environments -- Android, Python, React and React-Native. Each modality is summarized in the following table.

| Modality  | Resides on  | Initiated by  | Language | Compilation steps   |
|---|---|---|---|---|
| Android  | Phone  | Wakes up  |  Java |  `gradle build` |
| React Native  |  Phone | Wakes up  | Javascript  | `react-native build`  |
|  React | Browser  | User initiated  | Javascript  |  `react build` |
| Python scripts | Server  | Wakes up  |  Python | Copy over scripts  |

They are stored in the `algorithms` folder in the repository. Each modality has a sub-section which 

# Isolated Data Collection Environment