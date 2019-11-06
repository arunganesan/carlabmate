#! /usr/bin/env python3

class AlgorithmFunction:
    def __init__ (self, name, belongsto, outputinfo, inputinfo):
        self.name = name
        self.belongsto = belongsto
        self.outputinfo = outputinfo
        self.inputinfo = inputinfo
        

class Algorithm:
    def __init__ (self):
        self.functions = []

    def add_new_data(self, data_marshal):
        # check the information of the data
        return 0
    
    def output_data(self, data_marshal):
        if data_marshal.value is None:
            return

        # otherwise output this data.
        # using CL library
        # to output means to save it locally, and then periodically uploading it

class Information:
    def __init__ (self, name, datatype):
        self.name = name
        self.datatype = datatype

# Information statically created. 
class WorldAlignedAccel (Information):
    def __init__ (self):
        self.name = 'world-aligned-accel'
        self.datatype = [0.0]*3

class Fall (Information):
    def __init__ (self):
        self.name = 'fall'
        self.datatype = True


class DataMarshal:
    def __init__ (self):
        self.data = 1










class FallDetectBase (Algorithm):
    def __init__ (self):
        import libcarlab
        self.algorithm_functions = [
            libcarlab.AlgorithmFunction(libcarlab.Fall, [libcarlab.WorldAlignedAccel()])
        ]
        # self.algorithm_functions = [
        #     self.produce_fall, output = "fall", inp = ["world aligned accel"]]
     
    def add_new_data(self, data):
        # call super add_new_data(data)
        if type(data.info) is WorldAlignedAccel:
            self.output_data(self.produce_fall(data.value))

    def produce_fall (self, world_aligned_accel):
        print(world_aligned_accel)
        return None

   
class FallDetect (FallDetectBase):
    def produce_fall (self, world_aligned_accel):
        print(world_aligned_accel)
        return None

    
            
