package edu.umich.carlab.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import edu.umich.carlab.Constants;
import edu.umich.carlab.clog.CLog;
import edu.umich.carlab.utils.Utilities;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.Experiment_New_Version_Detected;
import static edu.umich.carlab.Constants.Experiment_New_Version_Last_Checked;

/**
 * Created by arunganesan on 3/20/18.
 */

public class CheckUpdate extends BroadcastReceiver {
    String TAG = "CheckUpdate";
    Context context;
    SharedPreferences prefs;
    int version;
    Response.Listener<String> gotVersionNum = new Response.Listener<String>() {
        @Override
        public void onResponse(String responseTripNum) {
            try {
                int latestVersionNumber = Integer.parseInt(responseTripNum);
                if (latestVersionNumber != version) {
                    prefs
                            .edit()
                            .putBoolean(Experiment_New_Version_Detected, true)
                            .putLong(Experiment_New_Version_Last_Checked, System.currentTimeMillis())
                            .apply();
                }
            } catch (Exception e) {
                CLog.e(TAG, "Error parsing JSON of list of uploaded receipts");
            }
        }
    };
    Response.ErrorListener gotVersionError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            CLog.e(TAG, "Error: " + error.getLocalizedMessage());
        }
    };

    /**
     * Make a volley request and get the latest.
     * Once we get it, then we can cancel the request.
     */
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        // Only contact Internet if we are on WiFi
        if (!Utilities.isConnectedAndWifi(context)) return;

        prefs = getDefaultSharedPreferences(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        String shortname = prefs.getString(Constants.Experiment_Shortname, null);
        this.version = prefs.getInt(Constants.Experiment_Version_Number, -1);
        if (shortname == null || this.version == -1) {
            CLog.e(TAG, "Shortname was null or version == -1");
            return;
        }


        // Get the list of currently uploaded receipts. Only upload ones that weren't successfully
        // uploaded and processed
        StringRequest myReq = new StringRequest(Request.Method.GET,
                Constants.VERSION_URL + "?shortname=" + shortname,
                gotVersionNum,
                gotVersionError) {
        };

        queue.add(myReq);
    }
}