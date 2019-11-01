package edu.umich.carlabui;

import android.app.Activity;
import android.view.ViewGroup;

import edu.umich.carlab.DataMarshal;

public interface Shadow {
    void addData(DataMarshal.DataObject d);
    void initializeVisualization(Activity a, ViewGroup vg);
}
