package edu.umich.carlab.loadable;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.Registry;

public abstract class Algorithm extends App {
    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    public static class Function {
        public List<Registry.Information> inputInformation;
        public String name;
        public Registry.Information outputInformation;
        public Class<? extends Algorithm> belongsTo;

        public Function (String n, Class<? extends Algorithm> bt, Registry.Information o, Registry.Information... i) {
            name = n;
            belongsTo = bt;
            outputInformation = o;
            inputInformation = Arrays.asList(i);
        }
    }
}
