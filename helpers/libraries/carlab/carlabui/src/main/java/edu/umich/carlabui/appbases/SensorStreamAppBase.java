package edu.umich.carlabui.appbases;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.hal.HardwareAbstractionLayer;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.PhoneSensors;

/**
 * A basic sensor stream app.
 *
 * This app accepts new data coming in from {@link #newData(DataMarshal.DataObject)} visualizes
 * them as streams in a real-time line graph. All incoming data is normalized to 0-1 to make the
 * visualization clearer.
 */

public class SensorStreamAppBase extends App {

    SensorStream streamComponent;

    public SensorStreamAppBase(CLDataProvider cl, Context context) {
        super(cl, context);
        streamComponent = new SensorStream(context);
    }

    public void addLineGraph(String device, String sensor) {
        streamComponent.addLineGraph(device, sensor);
    }

    public void newData(final DataMarshal.DataObject fullDataObject) {
        super.newData(fullDataObject);
        streamComponent.newData(fullDataObject);
    }

    @Override
    public View initializeVisualization(Activity parentActivity) {
       return streamComponent.initializeVisualization(parentActivity);
    }

    @Override
    public void destroyVisualization() {
       streamComponent.destroyVisualization();
    }
}
