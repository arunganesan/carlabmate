#! /usr/bin/env python3

from libcarlab.libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    mapmatch_function = AlgorithmFunction(
            "mapmatch",
            AlgorithmImpl,
            Registry.MapMatchedLocation, 
            [Registry.Location])


    def __init__ (self, gateway):
        super().__init__(gateway)
        self.latest_values = {}

     
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
        import requests
        import json

        maps_api_key = os.getenv('MAPSAPI')
        if maps_api_key is None:
            cprint("Please install a valid API key under MAPSAPI envirionment variable", 'white', 'on_red')
            return

        lat, lng = location
        url = 'https://roads.googleapis.com/v1/snapToRoads?interpolate=true&path={}&key={}'.format(
            '{},{}'.format(lat, lng),
            maps_api_key)
       
        response = requests.get(url)
        parsed = response.json()
        print(parsed['snappedPoints'])
        return ("Plymouth Road", 0.5)