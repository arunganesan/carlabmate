#! /usr/bin/env python3


class Info ():
    def __init__ (self, name):
        self.name = name
        self.implemented_by = []

    def node (self):
        return ('info', self.name)

class Impl ():
    def __init__ (self, name, implements, devices, requires=[]):
        self.name = name
        self.implements = indexed_information[implements]
        indexed_information[implements].implemented_by.append(self)

        if type(devices) == list:
            self.devices = devices
        else:
            self.devices = [devices]
        
        if type(requires) == list:
            self.requires = [indexed_information[r] for r in requires]
        else:
            self.requires = [indexed_information[requires]]
        
    def node (self):
        return ('impl', self.name)


information = [
    Info('car/speed'),
    Info('car/odometer'),
    Info('car/fuel'),
    Info('car/rpm'),
    Info('car/steering'),
    Info('car/gear'),
    Info('location'),
    Info('magnet'),
    Info('imu'),
    Info('aligned imu'),
]
indexed_information = {
    info.name: info
    for info in information
}

implementations = [
    # watchfone
    Impl('watchfone/speed', 'car/speed', 'android', ['imu', 'location']),
    Impl('watchfone/odometer', 'car/odometer', 'android', 'location'),
    Impl('watchfone/fuel', 'car/fuel', 'android', 'car/odometer'),
    Impl('watchfone/gear', 'car/gear', 'android', 'car/speed'),
    Impl('watchfone/steering', 'car/steering', 'android', ['car/speed', 'aligned imu']),
    Impl('watchfone/rpm', 'car/rpm', 'android', ['car/gear', 'car/speed']),

    # aligned IMU
    Impl('android/aligned imu', 'aligned imu', 'android', ['magnet', 'imu']),
    Impl('vsense/aligned imu', 'aligned imu', 'android', ['magnet', 'imu']),
    Impl('watchfone/imu', 'aligned imu', 'android', ['magnet', 'imu']),
    
    # location providers
    Impl('phone/gps', 'location', ['android', 'iphone']),
    Impl('react-native/gps', 'location', ['android', 'iphone']),
    Impl('react-native/dummy', 'location', ['android', 'iphone']),
    
    # core implementations
    Impl('core/magnet', 'magnet', ['android', 'iphone']),
    Impl('core/imu', 'imu', ['android', 'iphone']),

    # openxc implementations
    Impl('openxc/speed', 'car/speed', 'openxc'),
    Impl('openxc/steering', 'car/steering', 'openxc'),
    Impl('openxc/odometer', 'car/odometer', 'openxc'),
    Impl('openxc/fuel', 'car/fuel', 'openxc'),
    Impl('openxc/rpm', 'car/rpm', 'openxc'),
    Impl('openxc/gear', 'car/gear', 'openxc'),

     # obd implementations
    Impl('obd/speed', 'car/speed', 'obd'),
    Impl('obd/odometer', 'car/odometer', 'obd'),
    Impl('obd/fuel', 'car/fuel', 'obd'),
    Impl('obd/rpm', 'car/rpm', 'obd'),
]

indexed_implementation = {
    impl.name: impl
    for impl in implementations
}


# // implementations: [
# // { name: 'pothole patrol', implements: ['pothole'], requires: ['map matched location', 'aligned imu'], sensors: [] },
# // { name: 'steering wheel estimation', implements: ['steering wheel'], requires: ['map matched location', 'aligned imu', 'speed'], sensors: [] },
# // { name: 'speed limit monitor', implements: ['exceed speed limit'], requires: ['map matched location', 'speed'], sensors: [] },
# // { name: 'android:imu aligner', implements: ['aligned imu'], requires: ['imu'], sensors: [] },
# // { name: '-', implements: ['map matched location'], requires: ['location', 'imu'], sensors: [] },
# // { name: 'obfuscation', implements: ['location'], requires: ['location'], sensors: [] },
# // { name: 'spoofer', implements: ['imu', 'location'], requires: [], sensors: [] },


if __name__ == '__main__':
    import json
    
    json_data = {}
    json_data['information'] = {
        info.name: { 
            'name': info.name,
            'implemented_by': [i.name for i in info.implemented_by]
        }
        for info in information
    }


    json_data['implementations'] = {
        impl.name: {
            'name': impl.name,
            'supplies': impl.implements.name,
            'requires': [i.name for i in impl.requires],
            'devices': impl.devices
        }
        for impl in implementations
    }

    print(json.dumps(json_data))
    """
    { 
        information: {
            name: {
                name: string,
                implemented_by: []
            }
        },
        implementations: {
            name: { 
                name: string, 
                supplies: string,
                requires: [string]
            }
        }
    }
    """