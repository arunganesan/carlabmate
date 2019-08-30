#! /usr/bin/env python3

from networkx.algorithms import bipartite
from pprint import pprint

import matplotlib.pyplot as plt
import networkx as nx
import numpy as np

class Info ():
    def __init__ (self, name):
        self.name = name

class Impl ():
    def __init__ (self, name, implements, requires):
        self.name = name
        self.implements = implements
        
        if type(requires) == list:
            self.requires = requires
        else:
            self.requires = [requires]


information = []
implementations = []


# watchfone related
stubs = [
    # these are stubs -- low-level information
    # these are implemented by core libraries
    'phone/magnet',
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
    Impl('android/aligned imu', 'phone/aligned imu', ['phone/magnet', 'phone/imu']),
    Impl('vsense/aligned imu', 'phone/aligned imu', ['phone/magnet', 'phone/imu']),
    Impl('comp filter aligned imu', 'phone/aligned imu', ['phone/magnet', 'phone/imu']),
    
    # location providers
    Impl('phone/gps', 'location', 'phone/gps'),
    Impl('react-native/gps', 'location', 'phone/gps'),
    Impl('react-native/dummy', 'location', []),

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


def main():
    # create bipartite graph
    dg = nx.DiGraph()
    for info in information:
        dg.add_node(('info', info), type='information')
    
    for impl in implementations:
        dg.add_node(('impl', impl.name), type='implementation')
        dg.add_edge(('impl', impl.name), ('info', impl.implements))
        for info in impl.requires:
            dg.add_edge(('info', info), ('impl', impl.name))

    print(dg.nodes())
    print('----------------')
    print(dg.edges())
    print('----------------')
    print(bipartite.is_bipartite(dg))
    
    attributes = nx.get_node_attributes(dg, 'type')
    nodes = dg.nodes()

    info_y = 0
    impl_y = 0
    info_x = 0
    impl_x = 1
    _ystep = 0.15

    positioning = {}
    for n in nodes:
        if attributes[n] == 'information':
            positioning[n] = np.array([info_x, info_y])
            info_y += _ystep
        else:
            positioning[n] = np.array([impl_x, impl_y])
            impl_y += _ystep
    
    colors = [0 if attributes[n] == 'information' else 1 for n in nodes]
    nx.draw_networkx(dg, pos=positioning, node_color=colors, with_labels=False, node_size=75)
    
    labels = {}
    label_positioning = positioning
    for n in nodes:
        p = label_positioning[n]
        if attributes[n] == 'information':
            label_positioning[n] = [p[0]-0.5, p[1]]
            labels[n] = n[1]
        else:
            label_positioning[n] = [p[0]+0.5, p[1]]
            labels[n] = n[1]

    nx.draw_networkx_labels(
        dg, 
        pos=label_positioning, 
        font_size=8, 
        labels=labels)
    
    ax = plt.gca()
    ax.set_aspect('equal')
    print(ax.set_xlim(-1, 2))
    plt.draw()
    plt.savefig('network.png')
    

if __name__ == '__main__':
    main()
