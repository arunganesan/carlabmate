package edu.umich.carlab.recurring;

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
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.LIVE_MODE;

/**
 * Created by arunganesan on 3/20/18.
 */

public class UploadFiles extends BroadcastReceiver {
    String TAG = "FileUploader";
    Context context;
    static byte[] buffer = new byte[65536];

    /**
     * Uploads files.
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


        SharedPreferences sharedPrefs = getDefaultSharedPreferences(context);
        if (sharedPrefs.getBoolean(LIVE_MODE, false)) {
            // No need to upload if we are in live mode
            return;
        }

        // Only upload if we are on WiFi
        if (!Utilities.isConnectedAndWifi(context)) return;

        String UID = sharedPrefs.getString(Constants.UID_key, null);

        if (UID == null) {
            Log.e(TAG, "Upload UID is null. Trying again later.");
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        // Get the list of currently uploaded receipts. Only upload ones that weren't succesfully
        // uploaded and processed
        StringRequest myReq = new StringRequest(Request.Method.GET,
                Constants.LIST_UPLOADED_URL + "?uid=" + UID,
                gotListSuccess,
                volleyError) {
        };

        queue.add(myReq);
    }


    // https://stackoverflow.com/questions/555503/how-to-convert-hex-to-byte-for-the-following-program
    public static String asHex (byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;
        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10)
                strbuf.append("0");
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }


    // https://stackoverflow.com/questions/5564643/android-calculating-sha-1-hash-from-file-fastest-algorithm
    private static String getSHA1FromFileContent(String filename)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            InputStream fis = new FileInputStream(filename);
            int n = 0;
            while (n != -1)
            {
                n = fis.read(buffer);
                if (n > 0)
                {
                    digest.update(buffer, 0, n);
                }
            }
            byte[] digestResult = digest.digest();

            return asHex(digestResult);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    Response.Listener<String> gotListSuccess = new Response.Listener<String>() {
        @Override
        public void onResponse(String responseJSON) {
            try {
                JSONObject listOfReceipts = new JSONObject(responseJSON);
                File[] localFiles = Utilities.listAllTripsInOrder(context);
                MessageDigest md = MessageDigest.getInstance("SHA1");

                for (File localFile : localFiles) {
                    String sha1 = getSHA1FromFileContent(localFile.getAbsolutePath());
                    Log.v(TAG, "sha1" + sha1);
                    // Check if this hash was already uploaded. If so, delete it.
                    // Else, upload this file:
                    int tripid = Utilities.filenameToTripId(localFile, context);
                    if (listOfReceipts.has("" + tripid)) {
                        // Now we can dete it confidently
                        Log.v(TAG, "Confidently deleting file " + localFile.getName());
                        localFile.delete();
                    } else {
                        Utilities.uploadFile(localFile, context);
                    }
                }
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