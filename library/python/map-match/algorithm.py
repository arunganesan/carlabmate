#! /usr/bin/env python3

from libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    def __init__ (self):
        self.latest_values = {}

        self.mapmatch_function = AlgorithmFunction(
                    "mapmatch",
                    AlgorithmImpl,
                    Registry.MapMatchedLocation, 
                    [Registry.Location])
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []

        cprint('\tReceived information: {} = {}'.format(dobj.info, dobj.value), 'magenta')

        self.latest_values[dobj.info] = dobj.value
        
        if self.mapmatch_function.matches_required(dobj.info) and self.mapmatch_function.have_received_all_required_data(self.latest_values.keys()):
            retval = self.mapmatch(dobj.value)
            if retval is not None:
                return_values.append(DataMarshal(
                    Registry.MapMatchedLocation,
                    retval
                ))
            
        return return_values


# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self):
        super(AlgorithmImpl, self).__init__()

    def mapmatch (self, location: Registry.Location.datatype) -> Registry.MapMatchedLocation.datatype:
        # TODO implement
        return None


