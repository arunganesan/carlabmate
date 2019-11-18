#! /usr/bin/env python3.7

import jsmin, json, pprint
from termcolor import cprint

java_datatype_mapping = {
    'float[2]': 'Float2',
    'float': 'Float',
    'int': 'Integer',
    'float[9]': 'Float[]',
    'float[3]': 'Float3',
    'string': 'String',
}


python_datatype_mapping = {
    'float[2]': 'Tuple[float, float]',
    'float': 'float',
    'int': 'int',
    'float[9]': 'List[float]',
    'float[3]': 'Tuple[float, float, float]',
    'string': 'str',
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
PYTHON


SPEC

    "map-match": {
        "platform": "python",
        "functions": {
            "mapmatch": {
                "input": ["location"],
                "output": "map-mached-location"
            }
        }
    },


FUNC DEF
 self.mapmatch_function = AlgorithmFunction(
                    "mapmatch",
                    AlgorithmImpl,
                    Registry.MapMatchedLocation, 
                    [Registry.Location])



IVOKCATION
    if self.mapmatch_function.matches_required(dobj.info) and self.mapmatch_function.have_received_all_required_data(self.latest_values.keys()):
            retval = self.mapmatch(Registry.Location.datatype(dobj.value))
            if retval is not None:
                return_values.append(DataMarshal(
                    Registry.MapMatchedLocation,
                    retval
                ))
            
        return return_values
    

STUB:

# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self):
        super(AlgorithmImpl, self).__init__()

    def mapmatch (self, location: Registry.Location.datatype) -> Registry.MapMatchedLocation.datatype:
        # TODO implement
        return None

"""



"""
JAVA
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
