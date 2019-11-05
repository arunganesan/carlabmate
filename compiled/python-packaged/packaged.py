#! /usr/bin/env python3
import argparse
import libcarlab

"""
This is the wrapper script 

It is responsible for just a few things:

    0. Initialize and give life to algorithms
    1. Multiplex data around
    2. IO from gateway server
"""

# per algorithm stuff
MODULE = 'fall_detect'
CLASS = 'FallDetect'

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', choices=['fixed', 'random'])
    args = parser.parse_args()
    
    # load the module
    module = __import__(MODULE)
    clz = getattr(module.algorithm, CLASS)
    print(dir(clz))
    
    # get all functions of module
    
    # have user choose which function
    
    # and send random data to function
    

if __name__ == '__main__':
    main()
