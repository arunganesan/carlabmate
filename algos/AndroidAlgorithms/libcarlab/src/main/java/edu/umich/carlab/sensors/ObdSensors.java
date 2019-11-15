package edu.umich.carlab.sensors;


import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;

public class ObdSensors {
    public final static String DEVICE = "obd";

    public final static String ENGINE_LOAD = "engine_load";
    public final static String ENGINE_RPM = "engine_rpm";
    public final static String RUNTIME = "runtime";
    public final static String THROTTLE = "throttle";
    public final static String SPEED = "speed";
    public final static String FUEL_LEVEL = "fuel_level";
    public final static String DISTANCE_SINCE_CLEARED = "distance_since_code_cleared";
    public final static String DISTANCE_WITH_MIL = "distance_with_mil";

    final static String TAG = "ObdSensor";

    public static String[] listAllSensors() {
        return new String[]{
                ENGINE_LOAD,
                ENGINE_RPM,
                RUNTIME,
                THROTTLE,
                SPEED,
                FUEL_LEVEL,
                DISTANCE_SINCE_CLEARED,
                DISTANCE_WITH_MIL
        };
    }

    public static boolean validSensor(String name) {
        return (name.equals(ENGINE_LOAD)
                || name.equals(ENGINE_RPM)
                || name.equals(RUNTIME)
                || name.equals(THROTTLE)
                || name.equals(SPEED)
                || name.equals(FUEL_LEVEL)
                || name.equals(DISTANCE_SINCE_CLEARED)
                || name.equals(DISTANCE_WITH_MIL)
        );
    }


    public static ObdCommand nameToCommand(String sensor) {
        switch (sensor) {
            case ENGINE_RPM:
                return new RPMCommand();
            case RUNTIME:
                return new RuntimeCommand();
            case THROTTLE:
                return new ThrottlePositionCommand();
            case SPEED:
                return new SpeedCommand();
            case FUEL_LEVEL:
                return new FuelLevelCommand();
            case DISTANCE_SINCE_CLEARED:
                return new DistanceSinceCCCommand();
            case DISTANCE_WITH_MIL:
                return new DistanceMILOnCommand();
        }
        return null;
    }


    public static float responseToFloat(ObdCommand command) {
        if (command.getName().equals(new RPMCommand().getName())) {
            return ((RPMCommand) command).getRPM();
        } else if (command.getName().equals(new SpeedCommand().getName())) {
            return ((SpeedCommand) command).getImperialSpeed();
        } else if (command.getName().equals(new FuelLevelCommand().getName())) {
            return ((FuelLevelCommand) command).getFuelLevel();
        } else if (command.getName().equals(new DistanceMILOnCommand().getName())) {
            return ((DistanceMILOnCommand) command).getImperialUnit();
        } else if (command.getName().equals(new DistanceSinceCCCommand().getName())) {
            return ((DistanceSinceCCCommand) command).getImperialUnit();
        }
        return 0;
    }
}
