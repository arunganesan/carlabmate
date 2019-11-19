package carlab.obd_devices;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    Map<Registry.Information, Object> latestValues = new HashMap<>();

    public static Function readFuelLevel = new Function(
            "readFuelLevel",
            Algorithm.class,
            Registry.CarFuel,
            Registry.ObdFuel
    );


    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "obd-devices";
    }

    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);
        Registry.Information information = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        latestValues.put(information, dObject.value);


        // Can auto generate this given the definitions
        if (readFuelLevel.matchesRequired(information) &&
            readFuelLevel.haveReceivedAllRequiredData(latestValues.keySet())) {

            outputData(
                    Registry.CarFuel,
                    readFuelLevel((Float) latestValues.get(Registry.ObdFuel)));
        }
    }

    public abstract Float readFuelLevel (Float obdFuel);

}
