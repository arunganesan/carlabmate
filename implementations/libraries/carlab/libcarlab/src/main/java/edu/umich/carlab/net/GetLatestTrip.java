package edu.umich.carlab.net;

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

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by arunganesan on 3/20/18.
 */

public class GetLatestTrip extends BroadcastReceiver {
    String TAG = "GetLatestTrip";
    Context context;
    SharedPreferences prefs;
    Response.Listener<String> gotLatestTrip = new Response.Listener<String>() {
        @Override
        public void onResponse(String responseTripNum) {
            try {
                int tripOffset = Integer.parseInt(responseTripNum);
                prefs.edit().putInt(Constants.Trip_Id_Offset, tripOffset).apply();
                Utilities.cancelInit(context);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSON of list of uploaded receipts");
            }
        }
    };
    Response.ErrorListener gotLatestError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // Error!
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
        String UID = prefs.getString(Constants.UID_key, null);
        RequestQueue queue = Volley.newRequestQueue(context);

        // Get the list of currently uploaded receipts. Only upload ones that weren't successfully
        // uploaded and processed
        StringRequest myReq = new StringRequest(Request.Method.GET,
                Constants.LATEST_TRIP_URL + "?uid=" + UID,
                gotLatestTrip,
                gotLatestError) {
        };

        queue.add(myReq);
    }
}