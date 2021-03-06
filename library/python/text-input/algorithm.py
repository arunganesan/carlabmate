#! /usr/bin/env python3.7

from libcarlab.libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    accept_fuel_level_function = AlgorithmFunction(
                "accept_fuel_level",
                AlgorithmImpl,
                Registry.CarFuel, 
                [Registry.UserText],
                [Registry.PhoneNumber])

                
    def __init__ (self, gateway):
        super().__init__(gateway)
        self.latest_values = {}
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []
        

        self.latest_values[dobj.info] = dobj.value
        
        cprint('\tReceived information: {} = {}'.format(dobj.info, dobj.value), 'magenta')
        cprint('\tLatest value has keys: {}'.format(self.latest_values.keys()), 'blue')
        
        if self.accept_fuel_level_function.matches_required(dobj.info) and self.accept_fuel_level_function.have_received_all_required_data(self.latest_values.keys()):
            if dobj.info == Registry.UserText:
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
        try:
            val = float(user_text[0]['message'])
            return val
        except:
            print('Not a number')
            self.gateway.send_text("Not a valid fuel value. Please enter a valid number")
            return None



