#! /usr/bin/env python3.7
from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

import argparse
import json
import os

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'
specs = json.loads(jsmin(open(SPECS, 'r').read()))
registry = json.loads(jsmin(open(REGISTRY, 'r').read()))

SANDBOX_DIR = '../sandbox'
TEMPLATE_DIR = '{}/template'.format(SANDBOX_DIR)

def generate_strategy(strategy):
    strategy.setdefault('name', 'generated')
    ODIR = strategy['name']
    if not os.path.exists(ODIR):
        os.makedirs(ODIR)

    per_platform = {}
    for algfunc in strategy['functions']:
        algname = algfunc['algorithm']
        platform = specs[algname]['platform']

        expanded_func = {}
        funcspec = specs[algname]['functions'][algfunc['function']]
        if 'output' in funcspec:
            expanded_func['output'] = {funcspec['output']: registry[funcspec['output']]}
        
        if 'input' in funcspec:
            expanded_func['input'] = {
                    i: registry[i] for i in funcspec['input']}
        
        per_platform.setdefault(platform, {}) 
        per_platform[platform].setdefault(algname, {})
        per_platform[platform][algname][algfunc['function']] = expanded_func
    
    
    # copy template




if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser('strategy')
    args = parser.parse_args()
    strategy = json.loads(jsmin(open(args.strategy, 'r').read()))
    generate_strategy(strategy)


















# link server
"""
cd SANDBOX/linkserver
yarn install
./bin/rake db:migrate
"""

# react
"""
cd SANDBOX/react
npm install
npm install --save ../../../library/react/libcarlab
npm install --save ../../../library/react/user-input
"""

# android
"""
cd SANDBOX/android
echo "include ':app', ':libcarlab', ':android-passthroughs'" > settings.gradle
echo "rootProject.name='Packaged'" >> settings.gradle
echo "project(':libcarlab').projectDir = new File(settingsDir, '../../../library/android/libcarlab')" >> settings.gradle
echo "project(':android-passthroughs').projectDir = new File(settingsDir, '../../../library/android/android-passthroughs')" >> settings.gradle
"""


"""
cd SANDBOX/android/app
ff = open('build.gradle', 'r').read()
ff = ff.replace('/*DEPENDENCIES*/', "implementation project(':android-passthroughs')")
ofile = open('build.gradle', 'w')
ofile.write(ff)
ofile.close()

`build.gradle`.replace('template.application.id',  'carlab.green-gps')

cd SANDBOX/android
# (this might be necessary because we're linking to the same folder)
./gradlew build
APK is saved under: SANDBOX/android/app/build/outputs/apk/debug/
adb install -t APKFILE
"""


# Python
"""
ln -sn ../../../library/python/libcarlab
ln -sn ../../../library/python/obstacle-warning-python obstacle_warning_python
"""


# start everything 
"""
( cd carlabserver && ./bin/rails s -b 0.0.0.0 -p 1234 & )
( cd sandbox/$(PROJECT)/linkserver && ./bin/rails s -b 0.0.0.0 -p 8080 & )
"""


# every time you create a new user:
"""
( cd sandbox/$(PROJECT)/python && ./packaged.py & )
"""