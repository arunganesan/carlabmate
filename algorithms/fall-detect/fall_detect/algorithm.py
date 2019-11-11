#! /usr/bin/env python3

from libcarlab import *
from termcolor import cprint
import os, json

class FallDetectBase (Algorithm):
    def __init__ (self):
        self.produce_fall_function = AlgorithmFunction(
                    "produce_fall",
                    FallDetect,
                    Registry.Fall, 
                    [Registry.WorldAlignedAccel])
                
        self.check_user_ok_function = AlgorithmFunction(
                    "check_user_is_ok",
                    FallDetect,
                    Registry.FallSeverity, 
                    [Registry.Text])


     
    def add_new_data(self, info, value) -> List[Union[DataMarshal, None]]:
        return_values = []

        cprint('\tReceived information: {} = {}'.format(info.name, value), 'magenta')

        if info == Registry.WorldAlignedAccel:
            return_values.append(self.produce_fall(value))
        elif info == Registry.Text:
            return_values.append(self.check_user_is_ok(value))
        
        return return_values
   
class FallDetect (FallDetectBase):
    def __init__ (self):
        self.already_texted = False
        super(FallDetect, self).__init__()

    def check_user_is_ok (self, text):
        # parser it and output text
        print('Received text: {}'.format(text))

    def produce_fall (self, world_aligned_accel):
        for entry in world_aligned_accel:
            filename = entry['file']
            if not os.path.exists(filename):
                continue
            
            parsed_data = [
                json.loads(line) 
                for line in open(filename, 'r').readlines()
            ]

            for row in parsed_data:
                accel_value = json.loads(row['value'])
                if accel_value[0] > 10:
                    cprint('\tLarge value detected', 'red')
                    if not self.already_texted:
                        send_text('Are you ok?')
                        self.already_texted = True
        return None


def send_text(message="How was your drive? Enter 1 - 7 (1 being the worst)"):
    import requests, urllib.parse
    # call the guy
    texting_server = "http://localhost:3030/texting/schedule_text"
    result = requests.post(texting_server, 
        { 
            'number': '17343584745',
            'message': urllib.parse.quote(message),
            'serverport': 1234
        })
