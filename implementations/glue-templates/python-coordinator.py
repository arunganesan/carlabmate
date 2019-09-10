#! /usr/bin/env python3

import requests
import time

SLEEP_TIME = 5
UPLOAD_EVERY = 30


# USER-SPECIFIC CONFIG
USERID = '2'

MODULES = [
    # list each implementation script
    # e.g. 'how_was_your_day'
]

imported = {}
for module in MODULES:
    mod = __import__(module)
    imported[module] = mod

def main():
    last_uploaded = {}

    # Periodically cycle through and run each script
    while (True):
        for name, module in imported.items():
            module.main.main(USERID, test=False)

            # try uploading data
            last_uploaded.setdefault(name, 0)
            if time.time() > last_uploaded[name] + UPLOAD_EVERY:
                module.main.carlab.upload_data()
                last_uploaded[name] = time.time()
        
        time.sleep(SLEEP_TIME)

        


if __name__ == '__main__':
    main()
