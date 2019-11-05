class FallDetect:
    def __init__ (self):
        import libcarlab
        self.algorithm_functions = [
            libcarlab.AlgorithmFunction(libcarlab.Fall, [libcarlab.WorldAlignedAccel()])
        ]
        # self.algorithm_functions = [
        #     self.produce_fall, output = "fall", inp = ["world aligned accel"]]
    
    
    def produce_fall (self, world_aligned_accel):
        print(world_aligned_accel)
        return None

    
            
