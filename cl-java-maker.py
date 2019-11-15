#! /usr/bin/env python3.7

import jsmin, json, pprint
from termcolor import cprint

datatype_mapping = {
    'float[2]': 'Float2',
    'float': 'Float',
    'int': 'Integer',
    'float[9]': 'Float[]',
    'float[3]': 'Float3',
    'string': 'String',
}

def transform_variable_name (name):
    parts = name.split('-')
    return ''.join([p.capitalize() for p in parts])
    

algorithms = json.loads(jsmin.jsmin(open('specs.jsonc', 'r').read()))

for alg, details in algorithms.items():
    if details['platform'] == 'android':
        cprint(alg, 'magenta')
        # pprint.pprint(details)
        for func, func_details in details['functions'].items():
            cprint(func, 'cyan')
            cprint('\tInput', 'white')
            for info in func_details['input']:
                cprint('\t=>'+transform_variable_name(info), 'grey') 

            cprint('\tOutput', 'white')
            cprint('\t=>'+transform_variable_name(
                func_details['output']
            ), 'grey')



"""

"produceWorldPointingRotation": {
                "output": "world-pointing-rotation", 
                "input": ["gravity", "magnetometer"] 
            },

"world-pointing-rotation": {"type": "float[9]" },
"gravity": {"type": "float[3]", "sensor": true},
"magnetometer": {"type": "float[3]", "sensor": true},


Pseudocode:
    for each function
    get the return data type in Java
    get the inptu data types in java

    create the abstract stub
    create and full in Algorithm.Function constructor call
    add an if clause -- if we got data, call this function

Into:
    public abstract Float[] produceWorldPointingRotation (Float3 gravity, Float3 magnet);

    public static Function produceWorldPointingRotation =
            new Function("produceWorldPointingRotation", Algorithm.class,
                         Registry.WorldPointingRotation, Registry.Gravity, Registry.Magnetometer);

                         and 
    
     if (produceWorldPointingRotation.matchesRequired(information) &&
            produceWorldPointingRotation.haveReceivedAllRequiredData(latestValues.keySet())) {

            outputData(
                    // Output this info
                    Registry.WorldPointingRotation,

                    // Get value from this callback
                    produceWorldPointingRotation((Float3) latestValues.get(Registry.Gravity),
                                                 (Float3) latestValues.get(Registry.Magnetometer)));
        }
"""
