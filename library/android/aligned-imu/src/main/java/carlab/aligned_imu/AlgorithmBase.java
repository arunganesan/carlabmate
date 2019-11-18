package carlab.aligned_imu;

import android.content.Context;
import android.renderscript.Float3;

import java.util.HashMap;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;


public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    Map<Registry.Information, Object> latestValues = new HashMap<>();

    public static Function produceGravityAlignedGyro =
            new Function("produceWorldAlignedAccel", Algorithm.class, Registry.GravityAlignedGyro,
                         Registry.Gravity, Registry.Gyro);
    public static Function produceVehicleAlignedAccel =
            new Function("produceVehicleAlignedAccel", Algorithm.class,
                         Registry.VehicleAlignedAccel, Registry.Accel,
                         Registry.VehiclePointingRotation);
    public static Function produceVehiclePointingRotation =
            new Function("produceVehiclePointingRotation", Algorithm.class,
                         Registry.VehiclePointingRotation, Registry.Magnetometer, Registry.GPS,
                         Registry.Gravity);
    public static Function produceWorldAlignedAccel =
            new Function("produceWorldAlignedAccel", Algorithm.class, Registry.WorldAlignedAccel,
                         Registry.Accel, Registry.WorldPointingRotation);
    public static Function produceWorldAlignedGyro =
            new Function("produceWorldAlignedGyro", Algorithm.class, Registry.WorldAlignedGyro,
                         Registry.Gyro, Registry.WorldPointingRotation);
    public static Function produceWorldPointingRotation =
            new Function("produceWorldPointingRotation", Algorithm.class,
                         Registry.WorldPointingRotation, Registry.Gravity, Registry.Magnetometer);

    

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

        latestValues.put(information, dObject.value);


        // Can auto generate this given the definitions
        if (produceWorldPointingRotation.matchesRequired(information) &&
            produceWorldPointingRotation.haveReceivedAllRequiredData(latestValues.keySet())) {

            outputData(
                    // Output this info
                    Registry.WorldPointingRotation,

                    // Get value from this callback
                    produceWorldPointingRotation((Float3) latestValues.get(Registry.Gravity),
                                                 (Float3) latestValues.get(Registry.Magnetometer)));
        }
    }

    public abstract Float produceGravityAlignedGyro (Float3 gravity, Float3 gyro);

    public abstract Float3 produceVehicleAlignedAccel (Float3 accel, Float[] rm);

    public abstract Float[] produceVehiclePointingRotation (Float3 magnet, Float3 gps,
                                                            Float3 gravity);

    public abstract Float3 produceWorldAlignedAccel (Float3 accel, Float[] rm);

    public abstract Float3 produceWorldAlignedGyro (Float3 gyro, Float[] rm);

    public abstract Float[] produceWorldPointingRotation (Float3 gravity, Float3 magnet);

}
