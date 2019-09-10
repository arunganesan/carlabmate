#! /usr/bin/env python3

import time

SLEEP_TIME = 60

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
    # Periodically cycle through and run each script
    while (True):
        for name, module in imported.items():
            module.main.main(USERID, test=False)
        time.sleep(SLEEP_TIME)

if __name__ == '__main__':
    main()