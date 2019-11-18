package carlab.obd_devices;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;

public class Algorithm extends AlgorithmBase {
    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public Float readFuelLevel (Float obdFuel) {
        return obdFuel;
    }

}
