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
    def __init__ (self, name, implements, requires=[]):
        self.name = name
        self.implements = implements
        
        if type(requires) == list:
            self.requires = requires
        else:
            self.requires = [requires]


information = []
implementations = []


# watchfone related

information += [
    'car/speed',
    'car/odometer',
    'car/fuel',
    'car/rpm',
    'car/steering',
    'car/gear',


    'location',
    'aligned imu',
    'magnet',
    'imu',
]


implementations += [
    # watchfone
    Impl('watchfone/speed', 'car/speed', ['imu', 'location']),
    Impl('watchfone/odometer', 'car/odometer', 'location'),
    Impl('watchfone/fuel', 'car/fuel', 'car/odometer'),
    Impl('watchfone/gear', 'car/gear', 'car/speed'),
    Impl('watchfone/steering', 'car/steering', ['car/speed', 'aligned imu']),
    Impl('watchfone/rpm', 'car/rpm', ['car/gear', 'car/speed']),

    # aligned IMU
    Impl('android/aligned imu', 'aligned imu', ['magnet', 'imu']),
    Impl('vsense/aligned imu', 'aligned imu', ['magnet', 'imu']),
    Impl('watchfone/imu', 'aligned imu', ['magnet', 'imu']),
    
    # location providers
    Impl('phone/gps', 'location'),
    Impl('react-native/gps', 'location'),
    Impl('react-native/dummy', 'location'),

    # core implementations
    Impl('core/magnet', 'magnet'),
    Impl('core/imu', 'imu'),

    # openxc implementations
    Impl('openxc/speed', 'car/speed'),
    Impl('openxc/steering', 'car/steering'),
    Impl('openxc/odometer', 'car/odometer'),
    Impl('openxc/fuel', 'car/fuel'),
    Impl('openxc/rpm', 'car/rpm'),
    Impl('openxc/gear', 'car/gear'),
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

    assert bipartite.is_bipartite(dg), 'Not a bipartite graph'
    attributes = nx.get_node_attributes(dg, 'type')
    nodes = dg.nodes()


    min_y, max_y = 0, 5
    info_x, impl_x = 0, 2

    info_y = 0
    impl_y = 0
    info_ystep = (max_y - min_y) / len(information)
    impl_ystep = (max_y - min_y) / len(implementations)

    positioning = {}
    for n in nodes:
        if attributes[n] == 'information':
            positioning[n] = np.array([info_x, info_y])
            info_y += info_ystep
        else:
            positioning[n] = np.array([impl_x, impl_y])
            impl_y += impl_ystep
    
    colors = [0 if attributes[n] == 'information' else 1 for n in nodes]
    nx.draw_networkx(dg, pos=positioning, node_color=colors, with_labels=False, node_size=75)
    
    labels = {}
    label_positioning = positioning
    for n in nodes:
        p = label_positioning[n]
        if attributes[n] == 'information':
            label_positioning[n] = [p[0]-0.65, p[1]]
            labels[n] = n[1]
        else:
            label_positioning[n] = [p[0]+0.95, p[1]]
            labels[n] = n[1]

    nx.draw_networkx_labels(
        dg, 
        pos=label_positioning, 
        font_size=8, 
        labels=labels)
    
    ax = plt.gca()
    ax.set_aspect('equal')
    print(ax.set_xlim(info_x - 2, impl_x + 2))
    plt.draw()
    plt.savefig('network.png')
    

if __name__ == '__main__':
    main()
