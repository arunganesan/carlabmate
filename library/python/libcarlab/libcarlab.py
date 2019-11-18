import calendar
import os
import pickle
import requests
import time
from typing import *



class Information:
    def __init__(self, name, datatype):
        self.name = name
        self.datatype = datatype

    def __hash__(self):
        return hash(self.name)

    def __eq__(self, other):
        return (
            self.__class__ == other.__class__ and
            self.name == other.name
        )


class DataMarshal:
    def __init__(self, info: Information, value: Any):
        self.info = info
        self.value = value


class Algorithm:
    def __init__(self):
        self.functions = []

    def add_new_data(self, data_marshal) -> List[Union[DataMarshal, None]]:
        # check the information of the data
        return None

    def output_data(self, data_marshal):
        if data_marshal.value is None:
            return

        # otherwise output this data.
        # using CL library
        # to output means to save it locally, and then periodically uploading it


class AlgorithmFunction:
    def __init__(self,
                 name: str,
                 belongsto: Algorithm,
                 outputinfo: Information,
                 inputinfo: List[Information],
                 usesinfo: List[Information] = []):

        self.name = name
        self.belongsto = belongsto
        self.outputinfo = outputinfo
        self.inputinfo = inputinfo
        self.usesinfo = usesinfo
    
    def matches_required (self, info: Information) -> bool:
        return info in self.inputinfo or info in self.usesinfo
    
    def have_received_all_required_data (self, available_info: List[Information]) -> bool:
        for info in self.inputinfo:
            if info not in available_info:
                return False
        
        for info in self.usesinfo:
            if info not in available_info:
                return False
        
        return True

# Registry
class Registry:
    PhoneNumber = Information('phone-number', str)
    Location = Information('location', Tuple[int, int])
    MapMatchedLocation = Information('map-matched-location', Tuple[str, float])
    CarFuel = Information('car-fuel', float)
    CarModel = Information('car-model', str)
    GearModelFile = Information('gear-model-file', str)
    Sighting = Information('sighting', Tuple[float, Tuple[int, int]])
    SightingsMap = Information('sightings-map', Any)
    UserText = Information('user-text', str)


"""
Find some place to put this.
Both the session ariable and everything else has to be loaded in from the request

SESSION = 'dd2a4372516dab38535282070785853f'

def send_text(phonenumber, message="Msg"):
    import requests, urllib.parse
    # call the guy
    texting_server = "http://localhost:3030/texting/schedule_text"
    result = requests.post(texting_server, 
        { 
            'number': phonenumber,
            'message': urllib.parse.quote(message),
            'session': SESSION,
            'serverport': 1234
        })

"""


class LinkGatewayService:
    def __init__(self, session: str, required_info: List[Information], refers_info: List[Information], output_info: List[Information], save_filename, test=False):
        self.last_check_time = calendar.timegm(time.gmtime()) - 10000
        self.test = test
        self.required_info = required_info
        self.refers_info = refers_info
        self.output_info = output_info

        # XXX weird to harcode this here
        baseurl = 'http://localhost:3000/'
        self.fetch_url = baseurl + \
            'list?information={info}&session=%s&sincetime={time}' % session
        self.push_url = baseurl + 'add?information={info}&session=%s' % session
        self.latest_url = baseurl + \
            'latest?information={info}&session=%s' % session
        self.save_filename = save_filename

        if not os.path.exists(save_filename):
            ofile = open(save_filename, 'wb')
            pickle.dump({}, ofile)
            ofile.close()

        # for refers info, get the latest info of that and multiplex it in

    def check_new_info(self):
        new_data = {}

        for info in self.required_info:
            url = self.fetch_url.format(
                info=info.name,
                time=self.last_check_time,
            )

            results = requests.get(url)
            new_data[info] = results.json()

        self.last_check_time = calendar.timegm(time.gmtime())
        return new_data

    def initialize_state(self):
        new_data = {}

        for info in self.required_info:
            url = self.latest_url.format(info=info.name)
            results = requests.get(url)
            new_data[info] = results.json()
        self.last_check_time = calendar.timegm(time.gmtime())
        return new_data

    def output_new_info(self, info, value):
        existingData = pickle.load(open(self.save_filename, 'rb'))
        existingData.setdefault(info, [])
        existingData[info].append(value)
        ofile = open(self.save_filename, 'wb')
        pickle.dump(existingData, ofile)
        ofile.close()

    def upload_data(self):
        for info in self.output_info:
            existingData = pickle.load(open(self.save_filename, 'rb'))

            if info in existingData:
                result = requests.post(self.push_url.format(
                    info=info.name,
                ), {'message': existingData[info]})

                if result.status_code == 200:
                    del existingData[info]
                    ofile = open(self.save_filename, 'wb')
                    pickle.dump(existingData, ofile)
                    ofile.close()
