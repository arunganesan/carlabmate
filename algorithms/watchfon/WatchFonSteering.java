package edu.umich.carlab.watchfon_steering;

import android.content.Context;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlabui.appbases.SensorListAppBase;

import java.util.Map;


/**
 * Steering wheel estimation. It uses vehicle properties, and
 * the estimated speed.
 */
public class AppImpl {
    public Float steering (Float lastSpeed, Floast lastYaw) {
        Double steering = (double) (lastSpeed / lastYaw);
        steering = Math.asin(VEHICLE_LENGTH / steering);
        steering = STEERING_RATIO * steering;
        steering *= 180 / Math.PI;
        return steering;
    }
}
