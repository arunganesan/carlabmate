package edu.umich.carlab.clog;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Class which accepts data and writes to a file
 */

public class LogFileWriter {
    Context context;
    final String FixedFilename = "logs.json";
    final String TAG = "TripWriter";
    SimpleDateFormat dateFormatter;
    File saveFile;

    private BufferedWriter buf;



    public static File GetLogDir(Context context) {
        File tracesDir = context.getExternalFilesDir("logs");
        tracesDir.mkdirs();
        return tracesDir;
    }


    public LogFileWriter(Context context) {
        this.context = context;
        saveFile = new File(LogFileWriter.GetLogDir(context), FixedFilename);
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(gos);
            this.buf = new BufferedWriter(osw);

            Log.d(TAG, "Constructed the LogCSVWriter");
        } catch (Exception e) {
            Log.e(TAG, "LogCSVWriter constructor failed");
        }

    }


    /**
     *
     *
     */
    public synchronized void addData (List<CLogDatabaseHelper.Row> rows) {
        for (CLogDatabaseHelper.Row row : rows) {
            String json = row.toJSON().toString();
            try {
                buf.write(json, 0, json.length());
                buf.newLine();
            } catch (IOException e) {
                Log.e(TAG, "IO Exception");
            }
        }
    }



    public String saveFile() {
        try {
            buf.flush();
            buf.close();

            Log.d(TAG, "Flushed and closed");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return saveFile.getAbsolutePath();
    }
}
