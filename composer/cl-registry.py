#! /usr/bin/env python3.7
from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

import argparse
import json
import os

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'

ODIR = 'images'
if not os.path.exists(ODIR):
    os.makedirs(ODIR)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--platform', choices=['android', 'python', 'react'], default='android')
    args = parser.parse_args()

    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    registry_entries = []
    for infoname, details in registry.items():
        registry_entries.append(write_for_language(
            args.platform, infoname, details))

    print(TEMPLATES[args.platform] % '\n    '.join(registry_entries))


def write_for_language(platform, infoname, details):
    special_sensors = {
        'android': ['accel', 'gps', 'gravity', 'gyro', 'magnetometer', 'obd-fuel']
    }

    if infoname in special_sensors[platform]:
        if platform == 'android':
            sensor_type = {
                'accel': 'Sensor.TYPE_ACCELEROMETER',
                'gps': '1',
                'gravity': 'Sensor.TYPE_GRAVITY',
                'gyro': 'Sensor.TYPE_GYROSCOPE',
                'magnetometer': 'Sensor.TYPE_MAGNETIC_FIELD',
                'obd-fuel': '1'
            }

            sensor_type_string = {
                'accel': 'Sensor.STRING_TYPE_ACCELEROMETER',
                'gps': 'null',
                'gravity': 'Sensor.STRING_TYPE_GRAVITY',
                'gyro': 'Sensor.STRING_TYPE_GYROSCOPE',
                'magnetometer': 'Sensor.STRING_TYPE_MAGNETIC_FIELD',
                'obd-fuel': 'null'
            }

            devsens = {
                'accel': 'new DevSen(PhoneSensors.DEVICE, PhoneSensors.ACCEL)',
                'gps': 'new DevSen(PhoneSensors.DEVICE, PhoneSensors.GPS)',
                'gravity': 'new DevSen(PhoneSensors.DEVICE, PhoneSensors.GRAVITY)',
                'gyro': 'new DevSen(PhoneSensors.DEVICE, PhoneSensors.GYRO)',
                'magnetometer': 'new DevSen(PhoneSensors.DEVICE, PhoneSensors.MAGNET)',
                'obd-fuel': 'new DevSen(ObdSensors.DEVICE, ObdSensors.FUEL_LEVEL)'
            }

            return 'public static Information {formatted_name} = new Information("{infoname}", {datatype}.class, {sentype}, {stringsentype}, {devsen});'.format(
                formatted_name=VARIABLE_TRANSFORMATIONS[platform](infoname),
                infoname=infoname,
                datatype=DATATYPE_MAPPING[platform][details['type']],
                sentype=sensor_type[infoname],
                stringsentype=sensor_type_string[infoname],
                devsen=devsens[infoname]
            )

    return ENTRIES[platform].format(
        formatted_name=VARIABLE_TRANSFORMATIONS[platform](infoname),
        infoname=infoname,
        datatype=DATATYPE_MAPPING[platform][details['type']])


ANDROID_TEMPLATE = """
package edu.umich.carlab;

import android.hardware.Sensor;
import android.provider.ContactsContract;
import android.renderscript.Float2;
import android.renderscript.Float3;

import java.util.Arrays;

import edu.umich.carlab.sensors.ObdSensors;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.utils.DevSen;

public class Registry {
    %s

    public static String FormatString (DataMarshal.DataObject dataObject) {
        Information information = dataObject.information;
        Object value = dataObject.value;
        String valString = "";

        if (information.dataType.equals(Float3.class)) {
            Float3 obj = (Float3) value;
            valString = Arrays.toString(new Float[]{obj.x, obj.y, obj.z});
        } else if (information.dataType.equals(Float2.class)) {
            Float2 obj = (Float2) value;
            valString = Arrays.toString(new Float[]{obj.x, obj.y});
        } else if (information.dataType.equals(Float.class)) {
            valString = "" + value;
        } else if (information.dataType.equals(String.class)) {
            valString = (String) value;
        } else if (information.dataType.equals(Float[].class)) {
            valString = Arrays.toString((Float[]) value);
        }

        return valString;
    }

    public static Information DevSenToInformation(DevSen devSen) {
        if (devSen.device.equals(PhoneSensors.DEVICE)) {
            if (devSen.sensor.equals(PhoneSensors.MAGNET)) return Magnetometer;
            else if (devSen.sensor.equals(PhoneSensors.GYRO)) return Gyro;
            else if (devSen.sensor.equals(PhoneSensors.GRAVITY)) return Gravity;
            else if (devSen.sensor.equals(PhoneSensors.ACCEL)) return Accel;
            else if (devSen.sensor.equals(PhoneSensors.GPS)) return GPS;
        } else if (devSen.device.equals(ObdSensors.DEVICE)) {
            if (devSen.sensor.equals(ObdSensors.FUEL_LEVEL)) return ObdFuel;
        }

        return null;
    }




    public static class Information {
        public Class<?> dataType;
        public DevSen devSensor;
        public int lowLevelSensor;
        public String lowLevelSensorName;
        public String name;

        public Information (String n, Class<?> dt) {
            this(n, dt, -1, null, null);
        }

        public Information (String n, Class<?> dt, int lls, String llsn, DevSen ds) {
            name = n;
            dataType = dt;
            lowLevelSensor = lls;
            lowLevelSensorName = llsn;
            devSensor = ds;
        }

        @Override
        public boolean equals (Object other) {
            if (!other.getClass().equals(Information.class)) return false;
            return ((Information) other).name.equals(name);
        }
    }
}
"""


JAVA_ENTRY = 'public static Information {formatted_name} = new Information("{infoname}", {datatype}.class);'

java_datatype_mapping = {
    'float[2]': 'Float2',
    'float': 'Float',
    'int': 'Integer',
    'string,float': 'Pair',
    'float[9]': 'Float[]',
    'float[3]': 'Float3',
    'string': 'String',
    'list[float[3]]': 'List<Float[]>'
}


python_datatype_mapping = {
    'float[2]': 'Tuple[float, float]',
    'float': 'float',
    'int': 'int',
    'float[9]': 'List[float]',
    'float[3]': 'Tuple[float, float, float]',
    'string': 'str',
    'string,float': 'Tuple[str,float]',
    'list[float[3]]': 'List[List[float]]'
}


def android_transform_variable_name(name):
    if name == 'gps':
        return 'GPS'

    parts = name.split('-')
    return ''.join([p.capitalize() for p in parts])


ENTRIES = {
    'android': JAVA_ENTRY
}

TEMPLATES = {
    'android': ANDROID_TEMPLATE,
}

DATATYPE_MAPPING = {
    'android': java_datatype_mapping,
    'python': python_datatype_mapping
}

VARIABLE_TRANSFORMATIONS = {
    'android': android_transform_variable_name,
}

if __name__ == '__main__':
    main()
