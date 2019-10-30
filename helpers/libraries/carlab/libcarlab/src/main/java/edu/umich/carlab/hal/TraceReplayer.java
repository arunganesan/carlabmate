package edu.umich.carlab.hal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import edu.umich.carlab.CLService;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.ManualTrigger;
import edu.umich.carlab.io.DataDumpWriter;

import static edu.umich.carlab.Constants.LIVE_MODE;
import static edu.umich.carlab.Constants.Load_From_Trace_Duration_End;
import static edu.umich.carlab.Constants.Load_From_Trace_Duration_Start;
import static edu.umich.carlab.Constants.Load_From_Trace_Key;
import static edu.umich.carlab.Constants.ManualChoiceKey;
import static edu.umich.carlab.Constants.REPLAY_PERCENTAGE;
import static edu.umich.carlab.Constants.REPLAY_STATUS;
import static edu.umich.carlab.Constants.UID_key;

public class TraceReplayer implements Runnable {
    final int INITIAL_WAIT_TIME = 500;
    final String TAG = "TraceReplayer";
    final long broadcastUiUpdateEvery = 500L;
    final int STOP_TIME_PADDING = 1500;
    boolean liveMode = false;
    long lastUiBroadcast = 0L;
    CLService carlabService;
    File ifile;
    String tripID;
    List<DataMarshal.DataObject> traceData;
    SharedPreferences prefs;
    float specStartTime = -1,
            specEndTime = -1;

    public TraceReplayer(CLService carlabService, String filename, int tripID) {
        this.carlabService = carlabService;
        ifile = new File(filename);
        this.tripID = "" + tripID;
        DataDumpWriter dataDumpWriter = new DataDumpWriter(carlabService);
        traceData = dataDumpWriter.readData(ifile);
        prefs = PreferenceManager.getDefaultSharedPreferences(carlabService);
        liveMode = prefs.getBoolean(LIVE_MODE, false);

        specStartTime = prefs.getFloat(Load_From_Trace_Duration_Start, -1);
        specEndTime = prefs.getFloat(Load_From_Trace_Duration_End, -1);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(INITIAL_WAIT_TIME);
        } catch (Exception e) {
        }

        Long startTimeInMillis = System.currentTimeMillis();
        Long dataOffsetTime = traceData.get(0).time;
        Long sleepTime = 0L;

        String uid = prefs.getString(UID_key, null);
        if (uid == null && !liveMode) {
            String errorMessage = "UID is null. Something went wrong with replay";
            // TODO This will crash since this thread didn't call Looper.prepare()
            Toast.makeText(carlabService, errorMessage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, errorMessage);
            return;
        }

        DataMarshal.DataObject dataObject;

        long previousDataTime = 0;
        long currTime = 0;

        for (int i = 0; i < traceData.size(); i++) {
            currTime = System.currentTimeMillis();
            dataObject = traceData.get(i);
            previousDataTime = dataObject.time;

            // Uncomment below if the trace replaying lags too much
            dataObject.time = System.currentTimeMillis();

            carlabService.newData(dataObject);

            if (i < traceData.size() - 1) {
                try {
                    sleepTime = traceData.get(i + 1).time - previousDataTime;
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (Exception e) {
                }
            }

            if (currTime > lastUiBroadcast + broadcastUiUpdateEvery) {
                Intent intent = new Intent(REPLAY_STATUS);
                intent.putExtra(REPLAY_PERCENTAGE, (double) i / traceData.size());
                carlabService.sendBroadcast(intent);
                lastUiBroadcast = currTime;
            }
        }

        prefs
                .edit()
                .putString(Load_From_Trace_Key, null)
                .putFloat(Load_From_Trace_Duration_Start, -1)
                .putFloat(Load_From_Trace_Duration_End, -1)
                .putBoolean(ManualChoiceKey, false).commit();

        carlabService.sendBroadcast(new Intent(
                carlabService,
                ManualTrigger.class));
    }
}
