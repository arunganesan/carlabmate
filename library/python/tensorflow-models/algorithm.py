#! /usr/bin/env python3.7

from libcarlab.libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):


    get_gear_model_file_function = AlgorithmFunction(
            "get_gear_model_file",
            AlgorithmImpl,
            Registry.GearModelFile, 
            [],
            [Registry.CarModel])


    def __init__ (self):
        self.latest_values = {}

    
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []

        self.latest_values[dobj.info] = dobj.value
        
        cprint('	Received information: {} = {}'.format(dobj.info, dobj.value), 'magenta')
        cprint('	Latest value has keys: {}'.format(self.latest_values.keys()), 'blue')
        

        if self.get_gear_model_file_function.matches_required(dobj.info) and self.get_gear_model_file_function.have_received_all_required_data(self.latest_values.keys()):        
            if dobj.info in self.get_gear_model_file_function.inputinfo:
                retval = self.get_gear_model_file(['self.latest_values["car-model"]'])
                if retval is not None:
                    return_values.append(DataMarshal(
                        Registry.GearModelFile,
                        retval
                    ))


        return return_values


# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self):
        super(AlgorithmImpl, self).__init__()


    def get_gear_model_file (self, car_model) -> Registry.GearModelFile.datatype:
        # Write code here
        return None

