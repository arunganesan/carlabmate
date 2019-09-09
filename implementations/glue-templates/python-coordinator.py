#! /usr/bin/env python3

import subprocess
import time

SLEEP_TIME = 60 # wake up every 1 minute

# USER-SPECIFIC CONFIG
# USERID = ...

SCRIPTS = [
    # list each implementation script
]

def main():
    # Periodically cycle through and run each script
    while (True):
        for script in SCRIPTS:
            subprocess.call(['python3', script, '--userid', USERID])
        time.sleep(SLEEP_TIME)

if __name__ == '__main__':
    main()