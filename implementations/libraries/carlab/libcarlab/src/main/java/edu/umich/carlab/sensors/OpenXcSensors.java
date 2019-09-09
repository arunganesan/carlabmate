package edu.umich.carlab.sensors;

import com.openxc.measurements.*;

public class OpenXcSensors {
    public final static String DEVICE = "openxc";

    public final static String SPEED = "speed";
    public final static String STEERING = "steering";
    public final static String ENGINERPM = "engine_rpm";
    public final static String ODOMETER = "odometer";
    public final static String FUEL_SINCE_RESTART = "fuel_since_start";
    public final static String FUEL = "fuel";
    public final static String GEAR = "gear";
    public final static String ACCEL_PEDAL = "accel_pedal";
    public final static String BRAKE_PEDAL = "brake_status";

    final static String TAG = "OpenxcController";

    public static String[] listAllSensors() {
        return new String[]{
                SPEED,
                STEERING,
                ENGINERPM,
                ODOMETER,
                FUEL_SINCE_RESTART,
                FUEL,
                GEAR,
                ACCEL_PEDAL,
                BRAKE_PEDAL
        };
    }

    public static boolean validSensor(String name) {
        return (name.equals(SPEED)
                || name.equals(STEERING)
                || name.equals(ENGINERPM)
                || name.equals(ODOMETER)
                || name.equals(FUEL_SINCE_RESTART)
                || name.equals(FUEL)
                || name.equals(ACCEL_PEDAL)
                || name.equals(BRAKE_PEDAL)
                || name.equals(GEAR)
        );
    }


    public static Class nameToClass(String sensor) {
        switch (sensor) {
            case SPEED:
                return VehicleSpeed.class;
            case STEERING:
                return SteeringWheelAngle.class;
            case ENGINERPM:
                return EngineSpeed.class;
            case ODOMETER:
                return Odometer.class;
            case FUEL_SINCE_RESTART:
                return FuelConsumed.class;
            case FUEL:
                return FuelLevel.class;
            case ACCEL_PEDAL:
                return AcceleratorPedalPosition.class;
            case BRAKE_PEDAL:
                return BrakePedalStatus.class;
            case GEAR:
                return TransmissionGearPosition.class;
        }

        return null;
    }

    public static float convertUnitToFloat(String sensor, Measurement measurement) {
        switch (sensor) {
            case SPEED:
                VehicleSpeed speed = (VehicleSpeed) measurement;
                return (float) speed.getValue().doubleValue();
            case STEERING:
                SteeringWheelAngle steer = (SteeringWheelAngle) measurement;
                return (float) steer.getValue().doubleValue();
            case ENGINERPM:
                EngineSpeed engine = (EngineSpeed) measurement;
                return (float) engine.getValue().doubleValue();
            case ODOMETER:
                Odometer odo = (Odometer) measurement;
                return (float) odo.getValue().doubleValue();
            case FUEL_SINCE_RESTART:
                FuelConsumed cons = (FuelConsumed) measurement;
                return (float) cons.getValue().doubleValue();
            case FUEL:
                FuelLevel fuel = (FuelLevel) measurement;
                return (float) fuel.getValue().doubleValue();
            case ACCEL_PEDAL:
                AcceleratorPedalPosition accel = (AcceleratorPedalPosition) measurement;
                return (float) accel.getValue().doubleValue();
            case BRAKE_PEDAL:
                BrakePedalStatus brake = (BrakePedalStatus) measurement;
                boolean onOff = brake.getValue().booleanValue();
                return (onOff) ? 1 : 0;
            case GEAR:
                TransmissionGearPosition gear = (TransmissionGearPosition) measurement;
                TransmissionGearPosition.GearPosition gearPosition = gear.getValue().enumValue();

                switch (gearPosition) {
                    case REVERSE:
                        return -1;
                    case NEUTRAL:
                        return 0;
                    case FIRST:
                        return 1;
                    case SECOND:
                        return 2;
                    case THIRD:
                        return 3;
                    case FOURTH:
                        return 4;
                    case FIFTH:
                        return 5;
                    case SIXTH:
                        return 6;
                    case SEVENTH:
                        return 7;
                    case EIGHTH:
                        return 8;
                }
        }

        return 999;
    }
}
