#! /usr/bin/env python3

import networkx as nx

class Info ():
    def __init__ (self, name):
        self.name = name

class Impl ():
    def __init__ (self, name, implements, requires):
        self.name = name
        self.implements = implements
        self.requires = requires


information = []
implementations = []


# watchfone related
stubs = [
    # these are stubs -- low-level information
    # these are implemented by core libraries
    'phone/magnetometer',
    'phone/imu',
    'phone/gps',
    'openxc/speed',
    'openxc/steering',
    'openxc/fuel',
    'openxc/odometer',
    'openxc/gear',
    'openxc/rpm',
]

information += [
    'car/speed',
    'car/odometer',
    'car/fuel',
    'car/rpm',
    'car/steering',
    'car/gear',
    'location',
    'phone/aligned imu', 
] + stubs


implementations += [
    # watchfone
    Impl('watchfone/speed', 'car/speed', ['phone/imu', 'location']),
    Impl('watchfone/odometer', 'car/odometer', 'location'),
    Impl('watchfone/fuel', 'car/fuel', 'car/odometer'),
    Impl('watchfone/gear', 'car/gear', 'car/speed'),
    Impl('watchfone/steering', 'car/steering', ['car/speed', 'phone/aligned imu']),
    Impl('watchfone/rpm', 'car/rpm', ['car/gear', 'car/speed']),

    # aligned IMU
    Impl('android/aligned imu', 'phone/aligned imu', ['phone/magnetometer', 'phone/imu']),
    Impl('vsense/aligned imu', 'phone/aligned imu', ['phone/magnetometer', 'phone/imu']),
    Impl('comp filter aligned imu', 'phone/aligned imu', ['phone/magnetometer', 'phone/imu']),
    
    # location providers
    Impl('phone/gps', 'location', 'phone/gps'),
    Impl('react-native/gps', 'location', 'phone/gps'),
    Impl('react-native/dummy', 'location', ''),

    # openxc implementations
    Impl('openxc/speed', 'car/speed', 'openxc/speed'),
    Impl('openxc/steering', 'car/steering', 'openxc/steering'),
    Impl('openxc/odometer', 'car/odometer', 'openxc/odometer'),
    Impl('openxc/fuel', 'car/fuel', 'openxc/fuel'),
    Impl('openxc/rpm', 'car/rpm', 'openxc/rpm'),
    Impl('openxc/gear', 'car/gear', 'openxc/gear'),
]

# // implementations: [
# // { name: 'pothole patrol', implements: ['pothole'], requires: ['map matched location', 'aligned imu'], sensors: [] },
# // { name: 'steering wheel estimation', implements: ['steering wheel'], requires: ['map matched location', 'aligned imu', 'speed'], sensors: [] },
# // { name: 'speed limit monitor', implements: ['exceed speed limit'], requires: ['map matched location', 'speed'], sensors: [] },
# // { name: 'android:imu aligner', implements: ['aligned imu'], requires: ['imu'], sensors: [] },
# // { name: '-', implements: ['map matched location'], requires: ['location', 'imu'], sensors: [] },
# // { name: 'obfuscation', implements: ['location'], requires: ['location'], sensors: [] },
# // { name: 'spoofer', implements: ['imu', 'location'], requires: [], sensors: [] },

# // // Low-level values
# // { name: 'raw', implements: ['imu', 'location'], requires: [], sensors: ['imu', 'location'] },
# // { name: 'web-based-input', implements: ['location'], requires: [], sensors: ['web:input'] },
# // { name: 'phone-based-input', implements: ['location'], requires: [], sensors: ['phone:input'] },
# // ],


    

if __name__ == '__main__':
    main()