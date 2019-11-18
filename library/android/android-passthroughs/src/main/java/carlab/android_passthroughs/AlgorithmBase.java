package carlab.android_passthroughs;

import android.content.Context;
import android.renderscript.Float2;
import android.renderscript.Float3;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    public static Function getLocation = new Function(
            "getLocation",
            Algorithm.class,
            Registry.Location,
            Registry.GPS
    );


    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "android-passthrough";
    }

    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);
        String sensor = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        switch (sensor) {
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        /*
        TODO Need to only call the function IF it is statically loaded in this invocation
         */
        // Actually call values
    }

    public abstract Float2 getLocation (Float3 gps);

}
