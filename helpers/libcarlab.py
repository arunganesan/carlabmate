import calendar
import os
import pickle
import requests
import time
from typing import List, Union


class DataMarshal:
    def __init__(self, info: str, value):
        self.info = info
        self.value = value


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
                 refersinfo: List[Information] = []):

        self.name = name
        self.belongsto = belongsto
        self.outputinfo = outputinfo
        self.inputinfo = inputinfo
        self.refersinfo = refersinfo


# Registry
class Registry:
    Fall = Information('fall', True)
    WorldAlignedAccel = Information('world-aligned-accel', [0.0]*3)
    CarModel = Information('car-model', True)
    FallSeverity = Information('fall-severity', int)
    Text = Information('text', str)
    PhoneNumber = Information('phone-number', str)


class LinkGatewayService:
    def __init__(self, session: str, required_info: List[Information], refers_info: List[Information], output_info: List[Information], save_filename, test=False):
        self.last_check_time = calendar.timegm(time.gmtime()) - 10000
        self.test = test
        self.required_info = required_info
        self.refers_info = refers_info
        self.output_info = output_info
        baseurl = 'http://localhost:1234/'
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
