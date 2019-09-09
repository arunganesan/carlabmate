package edu.umich.carlab.clog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CLogDatabaseHelper extends SQLiteOpenHelper {
    Context context;
    static final String TAG = "DBHelper";
    static final String DATABASE_NAME = "CLog";
    static final String TABLE = "clog";
    static final int VERSION = 4;

    static final String TIME_COLUMN_NAME = "time";
    static final String TAG_COLUMN_NAME = "tag";
    static final String LEVEL_COLUMN_NAME = "level";
    static final String MESSAGE_COLUMN_NAME = "message";
    private static CLogDatabaseHelper instance = null;

    String shortname = null;

    public class Row {
        public int _id;
        public long time;
        public String tag, level, message;

        public JSONObject toJSON () {
            JSONObject obj = new JSONObject();
            try {
                obj.put("_id", _id);
                obj.put(TIME_COLUMN_NAME, time);
                obj.put(TAG_COLUMN_NAME, tag);
                obj.put(LEVEL_COLUMN_NAME, level);
                obj.put(MESSAGE_COLUMN_NAME, message);
            } catch (JSONException e) {
                Log.e(TAG, "Could not create JSON object.");
            }

            return obj;
        }


        public Row (Cursor res) {
            _id = res.getInt(res.getColumnIndex("_id"));
            time = res.getLong(res.getColumnIndex(TIME_COLUMN_NAME));
            tag = res.getString(res.getColumnIndex(TAG_COLUMN_NAME));
            level = res.getString(res.getColumnIndex(LEVEL_COLUMN_NAME));
            message  = res.getString(res.getColumnIndex(MESSAGE_COLUMN_NAME));
        }
    }

    public static CLogDatabaseHelper getInstance() {
        return instance;
    }

    public static void initializeIfNeeded(Context context) {
        if (instance == null) {
            instance = new CLogDatabaseHelper(context);
        }
    }

    private CLogDatabaseHelper(Context context) {
        super (context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    /**
     * Initialization functions.
     */
    @Override
    public void onCreate (SQLiteDatabase db) {
        Log.v(TAG, "Creating tables.");
        try {
            db.execSQL("create table " + TABLE + " (" +
                    "_id integer primary key autoincrement, " +
                    TIME_COLUMN_NAME + " integer, " +
                    TAG_COLUMN_NAME + " text, " +
                    LEVEL_COLUMN_NAME + " text," +
                    MESSAGE_COLUMN_NAME + " text)");
        } catch (SQLiteException e) {
            Log.e(TAG, "Table creation exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Deleting data on database upgrade.");
        db.execSQL("drop table if exists " + TABLE);
        onCreate(db);
    }

    @Override
    public synchronized void close() {
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            db.close();
            super.close();
        }
    }






    /***************************************************/
    /* Publicly exposed database actions               */
    /***************************************************/
    public void save_log (String tag, String level, String message) {
        ContentValues values = new ContentValues();
        values.put(TIME_COLUMN_NAME, System.currentTimeMillis());
        values.put(TAG_COLUMN_NAME, tag);
        values.put(LEVEL_COLUMN_NAME, level);
        values.put(MESSAGE_COLUMN_NAME, message);
        getWritableDatabase().insert(TABLE, null, values);
    }


    public int get_num_rows () {
        long numRows = DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE);
        return (int) numRows;
    }


    public void delete_before (long time) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int deleted = db.delete(TABLE, TIME_COLUMN_NAME + " <= ?", new String[]{"" + time});
            Log.v(TAG, "Deleted " + deleted + " from clogs");
        } catch (SQLiteException e) {
            Log.e(TAG, "Database delete failed.");
        }
    }

    /**
     * Gets the earliest N rows.
     *
     * <em>Eventually, we want this function to be able to filter
     * by name and sort by urgency</em>
     *
     * @param N the number of rows
     *
     *
     */
    public List<Row> get_rows (int N) {
        N = get_num_rows() < N ? get_num_rows() : N;
        List<Row> rows = new ArrayList<>();

        Cursor res = getReadableDatabase().rawQuery("select * from "
                + TABLE + " order by _id desc limit " + N, null);

        if (res.moveToFirst()) {
            do {
                rows.add(new Row(res));
            } while (res.moveToNext());
        }

        res.close();
        return rows;
    }

    // Thanks to http://stackoverflow.com/questions/1995320/how-to-backup-database-file-to-sdcard-on-android
    public String export_to_file(List<Row> rows) {
        LogFileWriter writer = new LogFileWriter(context);
        writer.addData(rows);
        String filename = writer.saveFile();
        return filename;
    }

}