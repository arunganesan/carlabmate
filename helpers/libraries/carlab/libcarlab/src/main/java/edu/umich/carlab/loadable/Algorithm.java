package edu.umich.carlab.loadable;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class Algorithm extends App {
    public List<AlgorithmSpecs.AppFunction> algorithmFunctions;

    public Algorithm(CLDataProvider cl, Context context) {
        super(cl, context);
    }
}
