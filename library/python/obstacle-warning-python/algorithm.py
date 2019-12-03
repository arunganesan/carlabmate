#! /usr/bin/env python3.7

from libcarlab.libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    
    def __init__ (self, gateway):
        super().__init__(gateway)

        self.latest_values = {}
        self.update_sightings_function = AlgorithmFunction(
            "update_sightings",
            AlgorithmImpl,
            Registry.SightingsMap, 
            [Registry.Sighting],
            [])



    
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []

        self.latest_values[dobj.info] = dobj.value
        
        cprint('	Received information: {} = {}'.format(dobj.info, dobj.value), 'magenta')
        cprint('	Latest value has keys: {}'.format(self.latest_values.keys()), 'blue')
        
        
        if self.update_sightings_function.matches_required(dobj.info) and self.update_sightings_function.have_received_all_required_data(self.latest_values.keys()):
            if dobj.info in self.update_sightings_function.inputinfo or dobj.info in self.update_sightings_function.usesinfo:
                retval = self.update_sightings(self.latest_values[Registry.Sighting])
                if retval is not None:
                    return_values.append(DataMarshal(
                        Registry.SightingsMap,
                        retval
                    ))


        return return_values





# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self, gateway):
        super(AlgorithmImpl, self).__init__(gateway)
    
    def update_sightings (self, sighting) -> Registry.SightingsMap.datatype:
        import pickle
        # at a high level: 
            # save this sighting in a COMMON FILE of sightings
            # then return that entire file. Simple simple simple
            # Irrespective of other people
        
        if sighting == None:
            return None
        SIGHTINGS_FILE = 'sightings.obj'
        if os.path.exists(SIGHTINGS_FILE):
            sightings_so_far = pickle.load(open(SIGHTINGS_FILE, 'rb'))
        else:
            sightings_so_far = []

        sightings_so_far.append(sighting)
        ofile = open(SIGHTINGS_FILE, 'wb')
        pickle.dump(sightings_so_far, ofile)
        ofile.close()

        return sightings_so_far

