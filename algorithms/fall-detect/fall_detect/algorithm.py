#! /usr/bin/env python3

from libcarlab import *
from termcolor import cprint
import os, json

class FallDetectBase (Algorithm):
    def __init__ (self):
        self.produce_fall_function = AlgorithmFunction(
                    "produce_fall",
                    FallDetect,
                    Fall, 
                    [WorldAlignedAccel, CarModel])
            
     
    def add_new_data(self, info, value) -> List[Union[DataMarshal, None]]:
        return_values = []

        print('Received information:  ', info, value)

        if info == WorldAlignedAccel:
            return_values.append(self.produce_fall(value))
            
        return return_values

    def produce_fall (self, world_aligned_accel):
        return None

   
class FallDetect (FallDetectBase):
    def __init__ (self):
        self.already_texted = False
        super(FallDetect, self).__init__()

    def produce_fall (self, world_aligned_accel):
        # print(world_aligned_accel)
        # I think this will return a gzip file. 
        # we need to load that file, parse it, etc etc
        # received data has:
        # dict[
        #   id, 
        #   file (string url), 
        #   message, 
        #   received (date), 
        #   person_id (no need), 
        #   information_id (no need)
        # ]

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
    from twilio.rest import Client
    import os

    # # Your Account Sid and Auth Token from twilio.com/console
    account_sid = os.environ['TWILIO_ACCOUNT_SID']
    auth_token = os.environ['TWILIO_AUTH_TOKEN']

    client = Client(account_sid, auth_token)

    message = client.messages \
                    .create(
                        body=message,
                        from_='+17344363993',
                        to='+17343584745'
                    )
