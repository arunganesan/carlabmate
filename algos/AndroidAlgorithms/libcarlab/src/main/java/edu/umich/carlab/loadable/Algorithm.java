package edu.umich.carlab.loadable;

import android.content.Context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.Registry;

public abstract class Algorithm extends App {
    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    public static class Function {
        public Class<? extends Algorithm> belongsTo;
        public Set<Registry.Information> inputInformation;
        public String name;
        public Registry.Information outputInformation;
        public Set<Registry.Information> usesInformation;

        public Function (String n, Class<? extends Algorithm> bt, Registry.Information o,
                         Registry.Information... inputinfos) {
            name = n;
            belongsTo = bt;
            outputInformation = o;
            inputInformation = new HashSet<>();
            inputInformation.addAll(Arrays.asList(inputinfos));
            usesInformation = new HashSet<>();
        }

        public boolean haveReceivedAllRequiredData (Set<Registry.Information> allAvailableInfo) {
            return allAvailableInfo.containsAll(inputInformation) &&
                   allAvailableInfo.containsAll(usesInformation);
        }

        public boolean matchesRequired (Registry.Information newInfo) {
            return inputInformation.contains(newInfo) || usesInformation.contains(newInfo);
        }
    }
}
