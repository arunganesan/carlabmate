package carlab.android_passthroughs;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;

public class Algorithm extends AlgorithmBase {
    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public Float getLocation () {
        return 0.0f;
    }

}
