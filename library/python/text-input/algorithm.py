#! /usr/bin/env python3

from libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    def __init__ (self):
        self.latest_values = {}

        self.accept_fuel_level_function = AlgorithmFunction(
                    "accept_fuel_level",
                    AlgorithmImpl,
                    Registry.CarFuel, 
                    [Registry.UserText],
                    [Registry.PhoneNumber])
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []

        cprint('\tReceived information: {} = {}'.format(dobj.info, dobj.value), 'magenta')

        self.latest_values[dobj.info] = dobj.value
        
        if self.accept_fuel_level_function.matches_required(dobj.info) and self.accept_fuel_level_function.have_received_all_required_data(self.latest_values.keys()):
            retval = self.accept_fuel_level(dobj.value)
            if retval is not None:
                return_values.append(DataMarshal(
                    Registry.CarFuel,
                    retval
                ))
            
        return return_values


# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self):
        super(AlgorithmImpl, self).__init__()

    def accept_fuel_level (self, user_text: Registry.UserText.datatype) -> Registry.CarFuel.datatype:
        print("GOT FUEL LEVEL FOR USERTEXT", user_text.value)
        return None


