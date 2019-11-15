package carlab.android_passthroughs;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;

public abstract class AlgorithmBase extends edu.umich.carlab.loadable.Algorithm {
    public static Function ReadFuelLevel = new Function(
            "getLocation",
            Algorithm.class,

            Registry.GPS,

            // XXX What should be input for this?
            Registry.Gravity, Registry.Magnetometer
    );


    public AlgorithmBase (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "android_passthrough";
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

    public abstract Float getLocation ();

}
