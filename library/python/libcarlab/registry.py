
from typing import *

class Information:
    def __init__(self, name, datatype):
        self.name = name
        self.datatype = datatype

    def __hash__(self):
        return hash(self.name)

    def __eq__(self, other):
        return (
            self.__class__ == other.__class__ and
            self.name == other.name
        )


# Registry
class Registry:
    MapMatchedLocation = Information('map-matched-location', Tuple[str,float])
    Location = Information('location', Tuple[float, float])
    CarSpeed = Information('car-speed', float)
    CarGear = Information('car-gear', int)
    CarFuel = Information('car-fuel', float)
    CarSteering = Information('car-steering', float)
    VehiclePointingRotation = Information('vehicle-pointing-rotation', List[float])
    WorldPointingRotation = Information('world-pointing-rotation', List[float])
    VehicleAlignedAccel = Information('vehicle-aligned-accel', Tuple[float, float, float])
    WorldAlignedAccel = Information('world-aligned-accel', Tuple[float, float, float])
    WorldAlignedGyro = Information('world-aligned-gyro', Tuple[float, float, float])
    GravityAlignedGyro = Information('gravity-aligned-gyro', float)
    GearModelFile = Information('gear-model-file', str)
    CarModel = Information('car-model', str)
    PhoneNumber = Information('phone-number', str)
    UserText = Information('user-text', str)
    Sighting = Information('sighting', Tuple[float, float, float])
    SightingsMap = Information('sightings-map', List[List[float]])
    GPS = Information('gps', Tuple[float, float, float])
    Accel = Information('accel', Tuple[float, float, float])
    Magnetometer = Information('magnetometer', Tuple[float, float, float])
    Gyro = Information('gyro', Tuple[float, float, float])
    Gravity = Information('gravity', Tuple[float, float, float])
    ObdFuel = Information('obd-fuel', float)

