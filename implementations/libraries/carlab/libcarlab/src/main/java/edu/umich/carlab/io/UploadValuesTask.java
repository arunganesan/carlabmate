package edu.umich.carlab.io;

import android.os.AsyncTask;
import android.util.Log;
import edu.umich.carlab.Constants;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by arunganesan on 2/27/18.
 */

public class UploadValuesTask extends AsyncTask {
    final String TAG = "UploadFile";
    File uploadFile;
    int experimentId;

    public UploadValuesTask(File uploadFile, int experimentId) {
        // Send to server
        // We will send this to our server
        // Maybe we will multiplex there but for now just send to our server
        this.uploadFile = uploadFile;
        this.experimentId = experimentId;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            URL url = new URL(Constants.DEFAULT_UPLOAD_URL + "?experimentId=" + this.experimentId);
            MultipartUtility mpu = new MultipartUtility(url);
            mpu.addFilePart("gzip", uploadFile);
            mpu.finish();
            Log.v(TAG, "Upload succeeded!");

        } catch (MalformedURLException mue) {

        } catch (IOException ieo) {
            Log.e(TAG, "Upload filed due to error: " + ieo.getMessage());
        }

        return null;
    }
}
