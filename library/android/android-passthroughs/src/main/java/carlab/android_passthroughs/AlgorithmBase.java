package carlab.android_passthroughs;

import android.content.Context;
import android.renderscript.Float2;
import android.renderscript.Float3;

import java.util.HashMap;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    Map<Registry.Information, Object> latestValues = new HashMap<>();

    public static Function getLocation = new Function(
            "getLocation",
            Algorithm.class,
            Registry.Location,
            Registry.GPS
    );


    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "android-passthroughs";
    }

    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);

        Registry.Information information = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        latestValues.put(information, dObject.value);


        // Can auto generate this given the definitions
        if (getLocation.matchesRequired(information) &&
            getLocation.haveReceivedAllRequiredData(latestValues.keySet())) {

            outputData(
                    Registry.Location,
                    getLocation((Float3) latestValues.get(Registry.GPS)));
        }
    }

    public abstract Float2 getLocation (Float3 gps);

}
