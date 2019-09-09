package edu.umich.carlab.clog;

import android.os.AsyncTask;
import android.util.Log;
import edu.umich.carlab.Constants;
import edu.umich.carlab.io.MultipartUtility;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Upload AsyncTask. Uploads the GZIP log file using MultipartUtility;
 */

public class UploadLogTask extends AsyncTask {
    private final String TAG = "UploadFile";
    private File uploadFile;
    private String UID, shortName;

    UploadLogTask(File uploadFile, String UID, String shortName) {
        this.uploadFile = uploadFile;
        this.UID = UID;
        this.shortName = shortName;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            URL url = new URL(Constants.UPLOAD_LOG_URL + "?uid=" + UID + "&shortname=" + shortName);
            MultipartUtility mpu = new MultipartUtility(url);
            mpu.addFilePart("gzip", uploadFile);
            mpu.finish();
            Log.v(TAG, "Upload succeeded!");
        } catch (MalformedURLException mue) {
            Log.e(TAG, "Malformed URL: " + mue.getLocalizedMessage());
        } catch (IOException ieo) {
            Log.e(TAG, "Upload filed due to error: " + ieo.getMessage());
        }

        return null;
    }
}
