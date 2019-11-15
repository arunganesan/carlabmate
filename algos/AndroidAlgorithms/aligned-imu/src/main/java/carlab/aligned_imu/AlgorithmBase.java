package carlab.aligned_imu;

import android.content.Context;
import android.renderscript.Float3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.Registry;
import edu.umich.carlab.sensors.PhoneSensors;

import static edu.umich.carlab.Registry.WorldAlignedGyro;
import static edu.umich.carlab.Registry.WorldPointingRotation;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    public abstract Float[] produceWorldPointingRotation (Float3 gravity, Float3 magnet);
    public abstract Float3 produceWorldAlignedGyro (Float3 gyro, Float[] rm);
    public abstract Float3 produceWorldAlignedAccel (Float3 accel, Float[] rm);
    public abstract Float[] produceVehiclePointingRotation (Float3 magnet, Float3 gps, Float3 gravity);
    public abstract Float3 produceVehicleAlignedAccel(Float3 accel, Float[] rm);
    public abstract Float produceGravityAlignedGyro(Float3 gravity, Float3 gyro);



    public static Function produceWorldPointingRotation = new Function(
            "produceWorldPointingRotation",
            Algorithm.class,
            WorldPointingRotation,
            Registry.Gravity, Registry.Magnetometer
    );


    public static Function produceWorldAlignedGyro = new Function(
            "produceWorldAlignedGyro",
            Algorithm.class,
            Registry.WorldAlignedGyro,
            Registry.Gyro, WorldPointingRotation
    );


    public static Function produceWorldAlignedAccel = new Function(
            "produceWorldAlignedAccel",
            Algorithm.class,
            Registry.WorldAlignedAccel,
            Registry.Accel, WorldPointingRotation
    );

    public static Function produceVehiclePointingRotation = new Function(
            "produceVehiclePointingRotation",
            Algorithm.class,
            Registry.VehiclePointingRotation,
            Registry.Magnetometer, Registry.GPS, Registry.Gravity
    );


    public static Function produceVehicleAlignedAccel = new Function(
            "produceVehicleAlignedAccel",
            Algorithm.class,
            Registry.VehicleAlignedAccel,
            Registry.Accel, Registry.VehiclePointingRotation
    );


    public static Function produceGravityAlignedGyro = new Function(
            "produceWorldAlignedAccel",
            Algorithm.class,
            Registry.GravityAlignedGyro,
            Registry.Gravity, Registry.Gyro
    );


    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "aligned-imu";
    }

    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);
        String sensor = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        switch (sensor) {
            case PhoneSensors.GRAVITY:
                lastGravity = (Float[]) dObject.value;
                break;
            case PhoneSensors.MAGNET:
                lastMagnet = (Float[]) dObject.value;
                break;
            case PhoneSensors.GYRO:
                lastGyro = (Float[]) dObject.value;
                break;
            case PhoneSensors.ACCEL:
                lastAccel = (Float[]) dObject.value;
                break;
            // case ROTATION:
            //     lastRotation = (Float[]) dObject.value;
            //     break;
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        /*
        TODO Need to only call the function IF it is statically loaded in this invocation
         */

        // TODO "sensor" is just a string. That's because DataObject passes a string around. It might be nice to pass Information objects around, like eveyrone else.

        Map<Registry.Information, Object> latestValues = new HashMap<>();


        // if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
        //     if (lastGravity != null && lastMagnet != null)
        //         outputData(
        //                 WorldPointingRotation.name,
        //                 produceWorldPointingRotation(lastMagnet, lastGravity));
        // } else if (sensor.equals(Registry.Gyro) || sensor.equals(WorldPointingRotation)) {
        //     if (lastRotation != null && lastGyro != null)
        //         outputData(WorldAlignedGyro.name, produceWorldAlignedGyro(lastGyro, lastRotation));
        // } else if (sensor.equals(PhoneSensors.ACCEL) || sensor.equals(WorldPointingRotation)) {
        //     if (lastAccel != null && lastRotation != null)
        //         outputData(ALIGNED_ACCEL, produceAlignedAccel(lastAccel, lastRotation));
        // }
    }

}
