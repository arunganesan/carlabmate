package carlab.vehicle_estimate;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {

    public static Function produceCarGear =
            new Function("produceCarGear", WatchFone.class, Registry.CarGear, Registry.CarSpeed);

    public static Function produceCarSpeed =
            new Function("produceCarSpeed", WatchFone.class, Registry.CarSpeed, Registry.WorldAlignedAccel,
                         Registry.GPS);
    public static Function produceCarSteering =
            new Function("produceCarSteering", WatchFone.class, Registry.CarSteering, Registry.CarSpeed,
                         Registry.WorldAlignedGyro);
    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfone";
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
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        // if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
        //     if (lastGravity != null && lastMagnet != null)
        //         outputData(ROTATION, produceRotation(lastMagnet, lastGravity));
        // } else if (sensor.equals(PhoneSensors.GYRO) || sensor.equals(ROTATION)) {
        //     if (lastRotation != null && lastGyro != null)
        //         outputData(ALIGNED_GYRO, produceAlignedGyro(lastGyro, lastRotation));
        // } else if (sensor.equals(PhoneSensors.ACCEL) || sensor.equals(ROTATION)) {
        //     if (lastAccel != null && lastRotation != null)
        //         outputData(ALIGNED_ACCEL, produceAlignedAccel(lastAccel, lastRotation));
        // }
    }

    public abstract Float produceCarFuel (Float[] car_odometer);

    public abstract Float produceCarGear (Float car_speed);

    public abstract Float produceCarOdometer (Float[] gps);

    public abstract Float produceCarRPM (Float[] car_gear, Float[] car_speed);

    public abstract Float produceCarSpeed (Float[] aligned_accel, Float[] gps);

    public abstract Float produceCarSteering (Float[] aligned_gyro, Float car_speed);
}
