package edu.umich.carlab;

import java.util.List;

import edu.umich.carlab.loadable.Algorithm;

public class Strategy {
    public List<Class<? extends Algorithm>> loadedAlgorithms;
    public List<Algorithm.Function> loadedFunctions;
    public List<Registry.Information> saveInformation;
}
