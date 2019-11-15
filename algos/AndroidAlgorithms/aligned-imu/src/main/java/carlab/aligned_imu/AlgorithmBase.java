package carlab.aligned_imu;

import android.content.Context;

import java.util.ArrayList;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.Registry;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    final String ALIGNED_ACCEL = "world-aligned-accel";
    final String ALIGNED_GYRO = "world-aligned-gyro";
    final String ROTATION = "rotation";

    public static Function ProduceRotation = new Function(
            "produceRotation",
            Algorithm.class,
            Registry.Rotation,
            Registry.Gravity, Registry.Magnetometer
    );


    public static Function ProduceAlignedGyro = new Function(
            "produceWorldAlignedGyro",
            Algorithm.class,
            Registry.WorldAlignedGyro,
            Registry.Gyro, Registry.Rotation
    );


    public static Function ProduceAlignedAccel = new Function(
            "produceWorldAlignedAccel",
            Algorithm.class,
            Registry.WorldAlignedAccel,
            Registry.Accel, Registry.Rotation
    );


    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "world_aligned_imu";
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
            case ROTATION:
                lastRotation = (Float[]) dObject.value;
                break;
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        /*
        TODO Need to only call the function IF it is statically loaded in this invocation
         */
        if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
            if (lastGravity != null && lastMagnet != null)
                outputData(ROTATION, produceRotation(lastMagnet, lastGravity));
        } else if (sensor.equals(PhoneSensors.GYRO) || sensor.equals(ROTATION)) {
            if (lastRotation != null && lastGyro != null)
                outputData(ALIGNED_GYRO, produceAlignedGyro(lastGyro, lastRotation));
        } else if (sensor.equals(PhoneSensors.ACCEL) || sensor.equals(ROTATION)) {
            if (lastAccel != null && lastRotation != null)
                outputData(ALIGNED_ACCEL, produceAlignedAccel(lastAccel, lastRotation));
        }
    }

    public abstract Float[] produceAlignedAccel (Float[] accel, Float[] rm);

    public abstract Float[] produceAlignedGyro (Float[] gyro, Float[] rm);

    public abstract Float[] produceRotation (Float[] m, Float[] g);
}
