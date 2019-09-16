package edu.umich.carlab.clog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import edu.umich.carlab.Constants;
import edu.umich.carlab.utils.Utilities;

import java.io.File;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by arunganesan on 3/20/18.
 */

public class UploadLog extends BroadcastReceiver {
    String TAG = "LogUploader";
    Context context;
    String UID, shortname;

    /**
     * Upload logs.
     * 0. Do we have WiFi and connectivity? If not, go back to sleep.
     * 1. Check with the server to get list of already uploaded files (using Volley) + the SHA1 or some stats on the file
     * 2. Then, check locally for all available trips
     * 3. If the local file SHA1 matches the already uploaded, then we can safely delete it
     * 4. If they are different or don't match. Try uploading again.
     * 5. Then go back to sleep.
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        // Only upload if we are on WiFi
        if (!Utilities.isConnectedAndWifi(context)) return;

        SharedPreferences sharedPrefs = getDefaultSharedPreferences(context);
        UID = sharedPrefs.getString(Constants.UID_key, "-1");
        shortname = sharedPrefs.getString(Constants.Experiment_Shortname, "undefined");

        RequestQueue queue = Volley.newRequestQueue(context);

        // Get the latest upload time
        StringRequest myReq = new StringRequest(Request.Method.GET,
                Constants.GET_LATEST_LOG_URL+ "?uid=" + UID,
                gotLatestLogTime,
                volleyError) {
        };

        queue.add(myReq);
    }


    Response.Listener<String> gotLatestLogTime = new Response.Listener<String>() {
        @Override
        public void onResponse(String latestTime) {
            try {
                // Get the latest and delete the DB
                Long latestTimeInt = Long.parseLong(latestTime);

                CLogDatabaseHelper.initializeIfNeeded(context);
                CLogDatabaseHelper db = CLogDatabaseHelper.getInstance();
                db.delete_before(latestTimeInt);
                if (db.get_num_rows() == 0) return;

                List<CLogDatabaseHelper.Row> rows = db.get_rows(1000);
                String filename = db.export_to_file(rows);
                UploadLogTask uploadLogTask= new UploadLogTask(new File(filename), UID, shortname);
                uploadLogTask.execute();

            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSON of list of uploaded receipts");
            }
        }
    };

    Response.ErrorListener volleyError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };
}