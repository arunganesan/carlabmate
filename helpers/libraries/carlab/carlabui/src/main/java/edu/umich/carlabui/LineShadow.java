package edu.umich.carlabui;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.DataMarshal;

public class LineShadow implements Shadow {
    final float MAX_TIMESCALE = 10;
    int[] colors = {Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN,
            Color.YELLOW, Color.WHITE};
    Map<String, LineDataSet> dataset;
    Map<String, List<Entry>> entries;
    LineChart lineChart;
    LineData lineData;
    Activity parentActivity;
    ViewGroup parentLayout;
    long starttime = 0, latesttime = 0;

    public void addData (DataMarshal.DataObject dataObject) {
        if (lineChart == null) return;
        String info = dataObject.information;
        Float[] values = (Float[]) dataObject.value;
        final long currtime = System.currentTimeMillis();
        long longtime = dataObject.time;
        if (starttime == 0) starttime = longtime;
        latesttime = Math.max(latesttime, longtime);
        double timeInSeconds = (longtime - starttime) / 1000.0;

        for (int i = 0; i < values.length; i++) {
            String key = String.format("%s[%d]", info, i);
            if (!dataset.containsKey(key)) {
                entries.put(key, new ArrayList<Entry>());
                dataset.put(key, new LineDataSet(entries.get(key), key));
                lineData.addDataSet(dataset.get(key));

                dataset.get(key).setColor(colors[dataset.size() % colors.length]);
                dataset.get(key).setDrawCircles(false);
                dataset.get(key).setLineWidth(5);
                dataset.get(key).setDrawValues(false);
                dataset.get(key).setMode(LineDataSet.Mode.CUBIC_BEZIER);
            }

            entries.get(key).add(new Entry((float) timeInSeconds, values[i]));

            // Notify and reload everything
            dataset.get(key).notifyDataSetChanged();
            lineData.notifyDataChanged();

            if (lineChart != null) {
                lineChart.notifyDataSetChanged();
                // Set end points
                float startPoint = Math.max(0,
                                            ((float) ((double) (latesttime - starttime) / 1000.0) -
                                             MAX_TIMESCALE));
                lineChart.setVisibleXRangeMaximum(MAX_TIMESCALE);
                lineChart.moveViewToX(startPoint);
            }
        }
    }

    public void initializeVisualization (Activity parentActivity, ViewGroup parentLayout) {
        this.parentActivity = parentActivity;
        this.parentLayout = parentLayout;

        entries = new HashMap<>();
        dataset = new HashMap<>();
        lineData = new LineData();
        lineChart = new LineChart(parentActivity);
        lineChart.setData(lineData);
        lineChart.invalidate();
        parentLayout.addView(lineChart);
    }
}
