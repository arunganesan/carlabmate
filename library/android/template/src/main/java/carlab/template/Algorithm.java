package carlab.template;

import android.content.Context;
import android.renderscript.Float2;
import android.renderscript.Float3;

import edu.umich.carlab.CLDataProvider;

public class Algorithm extends AlgorithmBase {
    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public Float2 getLocation (Float3 gps) {
        return new Float2(gps.x, gps.y);
    }
}
