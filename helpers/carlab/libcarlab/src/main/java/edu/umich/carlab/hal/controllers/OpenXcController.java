package edu.umich.carlab.hal.controllers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.openxc.NoValueException;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.OpenXcSensors;

import java.util.concurrent.Callable;

/**
 * Created by arunganesan on 2/19/18.
 */

public class OpenXcController {
    Context ctx;
    DataMarshal dm;
    VehicleManager mVehicleManager;

    long lastBindAttempt = 0;
    final long BIND_RETRY_PERIOD = 5000;

    final static String TAG = "OpenXCcontroller";

    //// Used to make sure we're not registering multiple to teh same listener
    //// The keys are Sensor Types (could be > 1 dimensional)


    public OpenXcController(Context ctx, DataMarshal dm) {
        this.ctx = ctx;
        this.dm = dm;
    }

    /**
     * Synchronous OpenXC polling
     * http://openxcplatform.com/android/api-guide.html
     * This helps us control the rate of polling these sensors.
     * Otherwise we get a binder overflow error because we get sensors too frequently
     * @param sensor
     * @return
     */
    public Callable<Float> createTask (final String sensor) {
        return new Callable<Float>()  {
            @Override
            public Float call() throws Exception {
                if (mVehicleManager == null) {
                    if (System.currentTimeMillis() > lastBindAttempt + BIND_RETRY_PERIOD) {
                        Intent intent = new Intent(ctx, VehicleManager.class);
                        ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                        lastBindAttempt = System.currentTimeMillis();
                    }
                    throw new VehicleManagerNotBound();
                }

                Measurement measurement;
                try {
                    Class clazz = OpenXcSensors.nameToClass(sensor);
                    measurement = (Measurement) mVehicleManager.get(clazz);
                } catch(NoValueException e) {
                    Log.w(TAG, "The vehicle may not have made the measurement yet");
                    throw new SensorError();
                } catch(UnrecognizedMeasurementTypeException e) {
                    Log.w(TAG, "The measurement type was not recognized");
                    throw new SensorNotFound();
                }

                long birthTime = measurement.getBirthtime(); // Not using this to sync between phone and OXC
                return OpenXcSensors.convertUnitToFloat(sensor, measurement);
            }
        };
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();
        }


        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };



    /**
     * If the sensor name is not valid
     */
    public class SensorNotFound extends Exception {}

    /**
     * If the sensor couldn't start for whatever reason
     */
    public class SensorError extends Exception {}

    public class VehicleManagerNotBound extends Exception {}

}
