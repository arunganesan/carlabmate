package edu.umich.carlab.hal;

import java.util.concurrent.Callable;

/**
 * This is an interface which each of the data controllers will implement. These are the basic
 * required commands they have to implement. The HAL can work with just these basic commands.
 *
 * TODO The createTask should return a Callable that returns TWO strings -- one for raw value and one for formatted value
 */



public interface JobMaker {
    public boolean validSensor(String sensor);
    public Callable<Float> createTask(String sensor);
}
