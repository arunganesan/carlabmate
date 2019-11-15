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
            Registry.WorldPointingRotation,
            Registry.Gravity, Registry.Magnetometer
    );


    public static Function produceWorldAlignedGyro = new Function(
            "produceWorldAlignedGyro",
            Algorithm.class,
            Registry.WorldAlignedGyro,
            Registry.Gyro, Registry.WorldPointingRotation
    );


    public static Function produceWorldAlignedAccel = new Function(
            "produceWorldAlignedAccel",
            Algorithm.class,
            Registry.WorldAlignedAccel,
            Registry.Accel, Registry.WorldPointingRotation
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

    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "aligned-imu";
    }


    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);
        Registry.Information information = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;


        Map<Registry.Information, Object> latestValues = new HashMap<>();
        latestValues.put(information, dObject.value);


        // Can auto generate this given the definitions
        if (produceWorldPointingRotation.matchesRequired(information)
            && produceWorldPointingRotation.haveReceivedAllRequiredData(latestValues.keySet())) {

            outputData(
                    // Output this info
                    Registry.WorldPointingRotation,

                    // Get value from this callback
                    produceWorldPointingRotation(
                        (Float3)latestValues.get(Registry.Gravity),
                        (Float3)latestValues.get(Registry.Magnetometer)));
        }




        /*
        TODO Need to only call the function IF it is statically loaded in this invocation
         */



        // TODO a more systematic and reasonable approach to map from JSON description to callback
        // TODO IF it is a "uses" relationship, we should call a setState() .. but that should already happen
        // IF it is a "uses" relationship, and we just got the data, we should still invoke it
        // In general, it is "invoke the guy" if each of the required information is NOT NULL.
        // If it is set somewhere.

        // TODO also -- don't call UNLESS we got new data.

        // BUT this COULD VERY EASILY be quite manual-looking. It can be auto-generated for sure.
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
