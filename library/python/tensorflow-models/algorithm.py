#! /usr/bin/env python3.7

from libcarlab.libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    
    def __init__ (self, gateway):
        super().__init__(gateway)

        self.latest_values = {}
        self.get_gear_model_file_function = AlgorithmFunction(
            "get_gear_model_file",
            AlgorithmImpl,
            Registry.GearModelFile, 
            [],
            [Registry.CarModel])



    
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []

        self.latest_values[dobj.info] = dobj.value
        
        cprint('	Received information: {} = {}'.format(dobj.info, dobj.value), 'magenta')
        cprint('	Latest value has keys: {}'.format(self.latest_values.keys()), 'blue')
        
        
        if self.get_gear_model_file_function.matches_required(dobj.info) and self.get_gear_model_file_function.have_received_all_required_data(self.latest_values.keys()):
            if dobj.info in self.get_gear_model_file_function.inputinfo or dobj.info in self.get_gear_model_file_function.usesinfo:
                retval = self.get_gear_model_file(self.latest_values[Registry.CarModel])
                if retval is not None:
                    return_values.append(DataMarshal(
                        Registry.GearModelFile,
                        retval
                    ))


        return return_values





# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self, gateway):
        super(AlgorithmImpl, self).__init__(gateway)


    def get_gear_model_file (self, car_model) -> Registry.GearModelFile.datatype:
        # based on the car model, we will return the pre-trained gear model file
        car_model = car_model['message']
        model_filenames = {
            'Ford Focus 2016': '1c2b1faf88413eaea1893205d01d65ee',
            'Ford Explorer 2016': '293c5527d05c5a9ddd4696fd608decd7',
            'Ford Lincoln MKZ 2018': '7081b3e360a38ec22c8faf38d997a895',
            'Ford Fiesta 2017': '816aa2c7fd4df5222e9f09495a2f8e81',
            'Ford Escape 2017': '885f1b75fd9adeacebc3c6da4487efa0',
        }

        if car_model in model_filenames:
            return '/gear-tensorflow-models/{}.jpg'.format(model_filenames[car_model])
        
        return None

