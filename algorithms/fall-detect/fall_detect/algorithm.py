#! /usr/bin/env python3

from libcarlab import *

class FallDetectBase (Algorithm):
    def __init__ (self):
        self.produce_fall_function = AlgorithmFunction(
                    "produce_fall",
                    FallDetect,
                    Fall, 
                    [WorldAlignedAccel])
            
     
    def add_new_data(self, data) -> List[Union[DataMarshal, None]]:
        return_values = []
        if type(data.info) is WorldAlignedAccel:
            return_values.append(self.produce_fall(data.value))
        return return_values

    def produce_fall (self, world_aligned_accel):
        return None

   
class FallDetect (FallDetectBase):
    def __init__ (self):
        super(FallDetect, self).__init__()

    def produce_fall (self, world_aligned_accel):
        print(world_aligned_accel)
        return None

    
            
