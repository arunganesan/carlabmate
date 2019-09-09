package edu.umich.carlabui.appbases;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.R;
import edu.umich.carlab.hal.HardwareAbstractionLayer;
import edu.umich.carlab.loadable.App;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by arunganesan on 2/17/18.
 */

public class SensorListAppBase extends App {
    public final String TAG = "SensorListTableView";
    boolean initialized = false;
    protected CLDataProvider cl;
    TableLayout dataTable;
    Map<String, TextView> tableRows;


    public SensorListAppBase(CLDataProvider cl, Context context) {
        super(cl, context);
    }

    TextView findOrCreateRow(String device, String sensor) {

        String key = device + "-" + sensor;
        if (tableRows.containsKey(key))
            return tableRows.get(key);
        else {
            TableRow newRow = new TableRow(context);
            TextView deviceTV = new TextView(context);
            TextView sensorTV = new TextView(context);
            TextView valueTV = new TextView(context);

            deviceTV.setTextSize(10);
            sensorTV.setTextSize(10);
            valueTV.setTextSize(10);

            int TABLE_ROW_MARGIN = 7;
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
                    TABLE_ROW_MARGIN);
            newRow.setLayoutParams(params);

            deviceTV.setText("[" + device + "] ");
            sensorTV.setText(sensor + ": ");
            sensorTV.setGravity(Gravity.RIGHT);
            newRow.addView(deviceTV);
            newRow.addView(sensorTV);
            newRow.addView(valueTV);

            dataTable.addView(newRow);
            tableRows.put(key, valueTV);
            return valueTV;
        }
    }

    @Override
    public void newData(DataMarshal.DataObject dataObject) {
        super.newData(dataObject);

        if (!initialized) return;
        
        if (dataObject.dataType != DataMarshal.MessageType.DATA)
            return;

        List<DataMarshal.DataObject> objects = HardwareAbstractionLayer.splitDataObjects(dataObject);
        for (final DataMarshal.DataObject dObject : objects) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv = findOrCreateRow(dObject.device, dObject.sensor);
                    tv.setText("" + dObject.value[0]);
                }
            });
        }
    }

    @Override
    public View initializeVisualization(Activity parentActivity) {
        super.initializeVisualization(parentActivity);
        LayoutInflater inflater = parentActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.tableview, null);
        tableRows = new HashMap<>();
        dataTable = view.findViewById(R.id.dataValues);
        initialized = true;
        return view;
    }

    @Override
    public void destroyVisualization() {
        tableRows.clear();
        dataTable.removeAllViews();
    }
}
