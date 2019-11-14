package edu.umich.carlab.hal.controllers;


import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.*;
import com.github.pires.obd.commands.engine.*;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO put description
 */
public class ObdConfig {
    public static Map<String, ObdCommand> cmds = new HashMap<>();


    public static void addAllDefault () {
        // Control
        addCommand(new ModuleVoltageCommand());
        addCommand(new EquivalentRatioCommand());
        addCommand(new DistanceMILOnCommand());
        addCommand(new DtcNumberCommand());

        // Engine
        addCommand(new LoadCommand());
        addCommand(new RPMCommand());
        addCommand(new RuntimeCommand());
        addCommand(new MassAirFlowCommand());
        addCommand(new ThrottlePositionCommand());

        // Fuel
        addCommand(new FindFuelTypeCommand());
        addCommand(new ConsumptionRateCommand());
        addCommand(new AirFuelRatioCommand());
        addCommand(new WidebandAirFuelRatioCommand());
        addCommand(new OilTempCommand());

        // Pressure
        addCommand(new BarometricPressureCommand());
        addCommand(new FuelPressureCommand());
        addCommand(new FuelRailPressureCommand());
        addCommand(new IntakeManifoldPressureCommand());

        // Temperature
        addCommand(new AirIntakeTemperatureCommand());
        addCommand(new AmbientAirTemperatureCommand());
        addCommand(new EngineCoolantTemperatureCommand());

        // Misc
        addCommand(new SpeedCommand());

    }

    public static Map<String, ObdCommand> getCommands() {
        return cmds;
    }

    private static void addCommand(ObdCommand cmd) {
        cmds.put(cmd.getName(), cmd);
    }

    public static void addCommandForIndex (int pidIndex) {
        if (pidIndex == 0x21) addCommand(new DistanceMILOnCommand());
        else if (pidIndex == 0x31) addCommand(new DistanceSinceCCCommand());
        else if (pidIndex == 0x01) addCommand(new DtcNumberCommand());
        else if (pidIndex == 0x44) addCommand(new EquivalentRatioCommand());
        else if (pidIndex == 0x42) addCommand(new ModuleVoltageCommand());

        else if (pidIndex == 0x43) addCommand(new AbsoluteLoadCommand());
        else if (pidIndex == 0x04) addCommand(new LoadCommand());
        else if (pidIndex == 0x10) addCommand(new MassAirFlowCommand());
        else if (pidIndex == 0x5C) addCommand(new OilTempCommand());
        else if (pidIndex == 0x0C) addCommand(new RPMCommand());
        else if (pidIndex == 0x1F) addCommand(new RuntimeCommand());
        else if (pidIndex == 0x11) addCommand(new ThrottlePositionCommand());
        else if (pidIndex == 0x44) addCommand(new AirFuelRatioCommand());
        else if (pidIndex == 0x5E) addCommand(new ConsumptionRateCommand());
        else if (pidIndex == 0x51) addCommand(new FindFuelTypeCommand());
        else if (pidIndex == 0x34) addCommand(new WidebandAirFuelRatioCommand());

        else if (pidIndex == 0x33) addCommand(new BarometricPressureCommand());
        else if (pidIndex == 0x0A) addCommand(new FuelPressureCommand());
        else if (pidIndex == 0x23) addCommand(new FuelRailPressureCommand());
        else if (pidIndex == 0x0B) addCommand(new IntakeManifoldPressureCommand());

        else if (pidIndex == 0x0F) addCommand(new AirIntakeTemperatureCommand());
        else if (pidIndex == 0x46) addCommand(new AmbientAirTemperatureCommand());
        else if (pidIndex == 0x05) addCommand(new EngineCoolantTemperatureCommand());
        else if (pidIndex == 0x0D) addCommand(new SpeedCommand());
    }
}