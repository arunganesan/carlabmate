#! /usr/bin/env python3

import argparse

# per algorithm stuff
MODULE = 'fall_detect'
CLASS = 'FallDetect'

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', choices=['fixed', 'random'])
    args = parser.parse_args()
    
    # load the module
    module = __import__(MODULE)
    clz = getattr(module.algorithm, CLASS)()
    #clz = Clz()
    print(dir(clz))
    # get all functions of module
    for function in clz.algorithm_functions:
        print(function)
    
    
    # have user choose which function
    # TODO
    # and send random data to function
    

if __name__ == '__main__':
    main()
