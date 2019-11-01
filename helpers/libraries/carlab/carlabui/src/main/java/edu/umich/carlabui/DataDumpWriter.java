package edu.umich.carlabui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.umich.carlab.DataMarshal;

/**
 * Class which accepts data and writes to a file
 */

public class DataDumpWriter {
    private final String TAG = "DumpWriter";
    Context context;
    List<DataMarshal.DataObject> dataObjects;
    private String filename;
    private SharedPreferences prefs;
    private File saveFile;

    public DataDumpWriter (Context c) {
        context = c;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        dataObjects = new ArrayList<>();
    }

    public static File GetDumpsDir (Context context) {
        File tracesDir = context.getExternalFilesDir("dumps");
        tracesDir.mkdirs();
        return tracesDir;
    }

    public void addData (DataMarshal.DataObject dataObject) {
        if (dataObjects != null)
            dataObjects.add(dataObject);
    }

    public String saveFile () {
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataObjects);
            oos.close();
            fos.close();
            Log.v(TAG, "Saved data dump. Total num objects = " + dataObjects.size());
        } catch (Exception e) {
            Log.e(TAG, "Failed to write file");
            e.printStackTrace();
        }

        dataObjects.clear();
        dataObjects = null;
        return saveFile.getName();
    }

    public List<DataMarshal.DataObject> readData (File ifile) {
        List<DataMarshal.DataObject> returnData = null;

        try {
            FileInputStream fis = new FileInputStream(ifile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            returnData = (List<DataMarshal.DataObject>) ois.readObject();
            ois.close();
            fis.close();
            Log.v(TAG, "Loaded data");
        } catch (Exception e) {
            Log.e(TAG, "Failed to write file");
        }

        return returnData;
    }

    public void startNewFile (String infoname) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        filename = infoname + "-" + dateFormatter.format(Calendar.getInstance().getTime()) + ".obj";
        saveFile = new File(DataDumpWriter.GetDumpsDir(context), filename);
        dataObjects = new ArrayList<>();
    }
}
