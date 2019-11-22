#! /usr/bin/env python3

from libcarlab.libcarlab import *
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
        
        # https://developers.google.com/maps/documentation/roads/snap with GPS points to get a set of GPS points along Place ID
        # https://developers.google.com/places/web-service/details with Place ID to get the road name
        # rough code to make API call: find_incline_using_api()
        
        return ("Plymouth Road", 0.5)


def find_incline_using_api(filename, lat, lng):
    import json
    import httplib
    from tqdm import tqdm
    import cPickle as pkl
    
    basename = os.path.basename(filename)
    basename, _ = os.path.splitext(basename)
    cache_file = '{}/{}.obj'.format(INCLINE_DIR, basename)
    if os.path.exists(cache_file):
        incline = pkl.load(open(cache_file))
        return incline
    else:
        # API key belongs to the RIDS Web project
        # And can be administered at https://console.cloud.google.com/google/maps-apis/apis/elevation-backend.googleapis.com/credentials?project=rids-web&duration=PT1H
        APIKEY = 'OMITTED'
        BASE = "/maps/api/elevation/json?locations={}&key=" + APIKEY
        
        incline = np.empty(len(lat))
        incline[:] = np.nan
        
        non_nan = ~np.isnan(lat) & ~np.isnan(lng)
        non_nan_lat = lat[non_nan]
        non_nan_lng = lng[non_nan]
        non_nan_inclines = np.zeros(len(non_nan_lat))
        
        conn = httplib.HTTPSConnection('maps.googleapis.com')
        
        AT_A_TIME = 200
        for idx in tqdm(list(range(0, len(non_nan_lat), AT_A_TIME)), desc='Downloading elevation'):
            _lat = non_nan_lat[idx:idx+AT_A_TIME]
            _lng = non_nan_lng[idx:idx+AT_A_TIME]
            _coords = zip(_lat, _lng)
            _all_lat_lng_str = ['{},{}'.format(r[0], r[1]) for r in _coords]
            url_params = '|'.join(_all_lat_lng_str)
            url = BASE.format(url_params)
            conn.request("GET", url)
            res = conn.getresponse()
            response = res.read()
            parsed = json.loads(response)

            if parsed['status'] != 'OK':
                cprint('Error with elevation result', 'white', 'on_red')
                print(parsed)
                exit(1)
            all_results = parsed['results']
            elevation = [result['elevation'] for result in all_results]
            non_nan_inclines[idx:idx+AT_A_TIME] = elevation
        
        
        incline[non_nan] = non_nan_inclines
        ofile = open(cache_file, 'w')
        pkl.dump(incline, ofile)
        ofile.close()
        return incline 


