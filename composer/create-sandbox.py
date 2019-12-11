#! /usr/bin/env python3.7
from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

import argparse
import shutil
import json
import os
import subprocess

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'
specs = json.loads(jsmin(open(SPECS, 'r').read()))
registry = json.loads(jsmin(open(REGISTRY, 'r').read()))

SANDBOX_DIR = 'sandbox'
TEMPLATE_DIR = '{}/template'.format(SANDBOX_DIR)
ODIR = 'localgen'
LIBDIR = '../../../../../library'

def generate_strategy(strategy, step):
    strategy.setdefault('name', 'generated')
    platformname = strategy['name']
    
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
    if step == 2:
        cprint('Copying template sandbox', 'green')
        sandbox = '{}/{}'.format(ODIR, platformname)
        shutil.copytree(TEMPLATE_DIR, sandbox, symlinks=True)
        
    
    # link server
    if step == 3:
        cprint('Setting up linking server', 'green')
        linkdir = '{}/linkserver'.format(sandbox)
        p = subprocess.Popen(['yarn', 'install'], cwd=linkdir); p.wait()
        p = subprocess.Popen(['./bin/rake', 'db:migrate'], cwd=linkdir); p.wait()


    # react
    if step == 4:
        if 'react' in per_platform:
            reactdir = '{}/react'.format(sandbox)
            cprint('Setting up React server', 'green')
            p = subprocess.Popen(['npm', 'install', '--save', '{}/react/libcarlab'.format(LIBDIR)], cwd=reactdir); p.wait()
            for algname in per_platform['react'].keys():
                moduledir = '{}/react/{}'.format(LIBDIR, algname)
                p = subprocess.Popen(['npm', 'install', '--save', moduledir], cwd=reactdir); p.wait()
            p = subprocess.Popen(['npm', 'install'], cwd=reactdir); p.wait()
            package_file = '{}/src/App.tsx'.format(reactdir)
            shutil.copyfile('{}/react.tsx'.format(platformname), package_file)



    # Python
    if step == 5:
        if 'python' in per_platform:
            pythondir = '{}/python'.format(sandbox)
            cprint('Setting up Python server', 'green')
            p = subprocess.Popen(['ln', '-sn', '{}/python/libcarlab'.format(LIBDIR)], cwd=pythondir); p.wait()
            for algname in per_platform['python'].keys():
                moduledir = '{}/react/{}'.format(LIBDIR, algname)
                p = subprocess.Popen(['ln', '-sn', moduledir], cwd=pythondir); p.wait()
            package_file = '{}/packaged.py'.format(pythondir)
            shutil.copyfile('{}/python.py'.format(platformname), package_file)

    # Android
    if step == 6:
        if 'android' in per_platform:
            android = '{}/android'.format(sandbox)
            cprint('Setting up Android server', 'green')
            lines = []
            gradlefile = '{}/settings.gradle'.format(android)
            gradle_modules = [':app', ':libcarlab']
            for algname in per_platform['android'].keys():
                gradle_modules.append(':' + algname)
            lines.append('include ' + ', '.join(["':{}'".format(mod) for mod in gradle_modules]))
            lines.append("rootProject.name='Packaged'")
            lines.append("project(':libcarlab').projectDir = new File(settingsDir, '{}/android/libcarlab')".format(LIBDIR))
            for algname in per_platform['android'].keys():
                lines.append("project(':{algname}').projectDir = new File(settingsDir, '{}/android/{algname}')".format(
                    LIBDIR, algname=algname
                ))
            ofile = open(gradlefile, 'w')
            ofile.write('\n'.join(lines))
            ofile.close()

            gradlefile = '{}/app/build.gradle'.format(android)
            ff = open(gradlefile, 'r').read()
            deps = []
            for algname in per_platform['android'].keys():
                deps.append("implementation project(':{}')".format(algname))
            ff = ff.replace('/*DEPENDENCIES*/', '\n'.join(deps))
            ff = ff.replace('template.application.id', 'carlab.{}'.format(platformname.replace('-', '')))
            ofile = open(gradlefile, 'w')
            ofile.write(ff)
            ofile.close()
            package_file = '{}/app/src/main/java/edu/umich/carlab/packaged/PackageCLService.java'.format(android)
            shutil.copyfile('{}/android.java'.format(platformname), package_file)

            p = subprocess.Popen(['./gradlew', 'assembleDebug'], cwd=android); p.wait()
            apkfile = '{}/app/build/outputs/apk/debug/app-debug.apk'.format(android)
            p = subprocess.Popen(['adb', 'install', '-t', apkfile], cwd=android); p.wait()

    



if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--step', type=int, choices=range(1, 8))
    parser.add_argument('strategy')
    args = parser.parse_args()


    platformname = args.strategy['name']
    sandbox = '{}/{}'.format(ODIR, platformname)


    if args.step == 1:
        subprocess.call(['./cl-package.py', args.strategy])
    else:
        strategy = json.loads(jsmin(open(args.strategy, 'r').read()))
        generate_strategy(strategy, args.step)
        linkdir = '{}/linkserver'.format(sandbox)
        p = subprocess.Popen(['yarn', 'install'], cwd=linkdir); p.wait()
        p = subprocess.Popen(['./bin/rake', 'db:migrate'], cwd=linkdir); p.wait()

        if args.step == 7:
            linkdir = '{}/linkserver'.format(sandbox)
            reactdir = '{}/react'.format(sandbox)
            subprocess.Popen('./bin/rails s -b 0.0.0.0 -p 8080'.split(), cwd=linkdir)
            subprocess.Popen('npm start'.split(), cwd=reactdir)
