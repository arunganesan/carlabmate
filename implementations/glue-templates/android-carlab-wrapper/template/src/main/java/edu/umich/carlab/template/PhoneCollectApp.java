package edu.umich.carlab.template;

import android.content.Context;
import android.util.Pair;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.loadable.SensorListAppBase;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.world_aligned_imu.MiddlewareImpl;


/**
 * Created by arunganesan on 6/24/18.
 */

public class PhoneCollectApp extends SensorListAppBase {
    public PhoneCollectApp(CLDataProvider cl, Context context) {
        super(cl, context);
        name = "PhoneCollectApp";
        description = "Only phone sensors";

        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.GPS));
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.ACCEL));
        sensors.add(new Pair<>(PhoneSensors.DEVICE, PhoneSensors.GYRO));

        sensors.add(new Pair<>(MiddlewareImpl.APP, MiddlewareImpl.ACCEL));
        sensors.add(new Pair<>(MiddlewareImpl.APP, MiddlewareImpl.GYRO));
    }
}
