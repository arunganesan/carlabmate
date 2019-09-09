#! /usr/bin/env python3

import haversine
import json
import os
import pickle
import time

class libcarlab ():
    def __init__ (self):
        '1'
    
    def check_new_info (self):
        return []
    
    def output_new_info (self, name, value):
        # save it to the outbox file
        return True

LOCALFILE = 'local.db'
LAST_TEXTED_TIME = 'LAST TEXTED TIME'
HOME_WORK = 'home work location'
CURRENT_LOC = 'current location'
TEXT_EVERY = 5*60 # 5 minutes

def main():
    storage = {}

    if os.path.exists(LOCALFILE):
        storage = pickle.load(open(LOCALFILE, 'rb'))
    
    storage.setdefault(LAST_TEXTED_TIME, 0)
    storage.setdefault(HOME_WORK, None)
    storage.setdefault(CURRENT_LOC, None)

    cl = libcarlab()
    
    # check if there is any new info
    for name, info in cl.check_new_info():
        if name == 'home-work':
            storage[HOME_WORK] = json.loads(info)
        
        if name == 'location':
            storage[CURRENT_LOC] = json.loads(info)
        
        # take action if needed
        if storage[HOME_WORK] != None and storage[CURRENT_LOC] != None:
            # Check if they are close to each other
            if haversine.haversine(
                (storage[HOME_WORK]['latitude'], storage[HOME_WORK]['longitude']),
                (storage[CURRENT_LOC]['latitude'], storage[CURRENT_LOC]['longitude'])
            ) < 0.1:
                print("Close enough - reay to text")
                ltt = storage[LAST_TEXTED_TIME]
                if time.time() > ltt + TEXT_EVERY:
                    # output info if needed
                    " send text! "
                    cl.output_new_info('mood', 'happy ¬me')
                    storage[LAST_TEXTED_TIME] = time.time()

    # go back to sleep
    ofile = open(LOCALFILE, 'wb')
    pickle.dump(storage, ofile)
    ofile.close()

if __name__ == '__main__':
    # test it with dummy data
    main()

    from twilio.rest import Client
    import os

    # Your Account Sid and Auth Token from twilio.com/console
    account_sid = os.environ['TWILIO_ACCOUNT_SID']
    auth_token = os.environ['TWILIO_AUTH_TOKEN']

    client = Client(account_sid, auth_token)

    # message = client.messages \
    #                 .create(
    #                     body="How was your day? Enter 1 - 7 (1 being the worst)",
    #                     from_='+17344363993',
    #                     to='+17343584745'
    #                 )

    # message = client.messages.create(
    #                           body='Hello there!',
    #                           from_='+17344363993',
    #                           media_url=['https://demo.twilio.com/owl.png'],
    #                           to='+17343584745'
    #                       )
    
    print(message.sid)