package edu.umich.carlab.trips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Some code taken from https://github.com/wdkapps/FillUp
 */
public class TripLog {

    /// the database version number
    public static final int DATABASE_VERSION = 6;
    /// the name of the database
    public static final String DATABASE_NAME = "tripslog.db";
    /// a tag string for debug logging (the name of this class)
    private static final String TAG = TripLog.class.getName();
    /// database table names
    private static final String RECORDS_TABLE = "Records";
    /// SQL commands to delete the database
    public static final String[] DATABASE_DELETE = new String[]{
            "drop table if exists " + RECORDS_TABLE + ";",
    };
    /// column names for RECORDS_TABLE
    private static final String RECORD_ID = "id";
    private static final String RECORD_START_DATE = "startDate";
    private static final String RECORD_END_DATE = "endDate";
    private static final String RECORD_SURVEY_RESPONSE = "surveyResponse";
    private static final String RECORD_SURVEY_UPLOADED = "surveyUploaded";

    /// SQL commands to create the database
    public static final String[] DATABASE_CREATE = new String[]{
            "create table " + RECORDS_TABLE + " ( " +
                    RECORD_ID + " integer primary key autoincrement, " +
                    RECORD_START_DATE + " integer not null, " +
                    RECORD_END_DATE + " integer, " +
                    RECORD_SURVEY_RESPONSE + " text, " +
                    RECORD_SURVEY_UPLOADED + " boolean default 0" +
                    ");"
    };
    /// array of all column names for RECORDS_TABLE
    private static final String[] RECORDS_TABLE_COLUMNS = new String[]{
            RECORD_ID,
            RECORD_START_DATE,
            RECORD_END_DATE,
            RECORD_SURVEY_RESPONSE,
            RECORD_SURVEY_UPLOADED
    };
    /// singleton instance
    private static TripLog instance;
    /// context of the instance creator
    private final Context context;
    /// a helper instance used to open and close the database
    private final TripLogOpenHelper helper;
    /// the database
    private final SQLiteDatabase db;

    private TripLog(Context context) {
        this.context = context;
        this.helper = new TripLogOpenHelper(this.context);
        this.db = helper.getWritableDatabase();
    }

    /**
     * DESCRIPTION:
     * Returns a single instance, creating it if necessary.
     *
     * @return GasLog - singleton instance.
     */
    public static TripLog getInstance(Context context) {
        if (instance == null) {
            instance = new TripLog(context);
        }
        return instance;
    }

    int getMaxID() {
        Cursor cursor = db.rawQuery("SELECT MAX(" + RECORD_ID + ") FROM " + RECORDS_TABLE, null);
        cursor.moveToFirst();
        int maxIndex = cursor.getInt(0);
        cursor.close();
        return maxIndex;
    }

    public TripRecord startTrip(int minOffset) {
        final String tag = TAG + ".createRecord()";

        try {
            int maxIndex = getMaxID();
            TripRecord record = new TripRecord();
            if (maxIndex < minOffset) {
                record.setID(minOffset + 1);
            } else {
                record.setID(maxIndex + 1);
            }

            db.insertOrThrow(
                    RECORDS_TABLE,
                    null,
                    getContentValues(record)
            );

            return record;
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }

        return null;
    }

    /**
     * DESCRIPTION:
     * Convenience method to test assertion.
     *
     * @param assertion - an asserted boolean condition.
     * @param tag       - a tag String identifying the calling method.
     * @param msg       - an error message to display/log.
     * @throws RuntimeException if the assertion is false
     */
    private void ASSERT(boolean assertion, String tag, String msg) {
        if (!assertion) {
            String assert_msg = "ASSERT failed: " + msg;
            Log.e(tag, assert_msg);
            throw new RuntimeException(assert_msg);
        }
    }

    /**
     * DESCRIPTION:
     * Updates a trip record in the log.
     *
     * @param record - the TripRecord to update.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean updateRecord(TripRecord record) {
        final String tag = TAG + ".updateRecord()";
        ASSERT((record.getID() != null), tag, "record id cannot be null");
        boolean success = false;
        try {
            ContentValues values = getContentValues(record);
            values.remove(RECORD_ID);
            values.put(RECORD_SURVEY_RESPONSE, "unused");
            String whereClause = RECORD_ID + "=" + record.getID();
            int count = db.update(RECORDS_TABLE, values, whereClause, null);
            success = (count > 0);
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return success;
    }

    private List<TripRecord> filterRowsBy(String whereClause) {
        final String tag = TAG + ".readAllRecords()";
        List<TripRecord> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            String orderBy = RECORD_START_DATE;
            cursor = db.query(
                    RECORDS_TABLE,
                    RECORDS_TABLE_COLUMNS,
                    whereClause,
                    null, null, null,
                    orderBy,
                    null
            );

            // create a list of TripRecords from the data
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        TripRecord record = getRecordFromCursor(cursor);
                        list.add(record);
                    } while (cursor.moveToNext());
                }
            }

        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
            list.clear();
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }


    public List<TripRecord> readNotUploadedAndFilled() {
        return filterRowsBy(RECORD_SURVEY_UPLOADED + " < 1 AND " + RECORD_SURVEY_RESPONSE + " IS NOT NULL");
    }

    public List<TripRecord> readUnfilledSurveys() {
        return filterRowsBy(RECORD_SURVEY_RESPONSE + " IS NULL");
    }


    /**
     * DESCRIPTION:
     * Convenience method to create a TripRecord instance from values read
     * from the database.
     *
     * @param c - a Cursor containing results of a database query.
     * @return a GasRecord instance (null if no data).
     */
    private TripRecord getRecordFromCursor(Cursor c) {
        final String tag = TAG + ".getRecordFromCursor()";
        TripRecord record = null;
        if (c != null) {
            record = new TripRecord();
            int id = c.getInt(c.getColumnIndex(RECORD_ID));
            long startDate = c.getLong(c.getColumnIndex(RECORD_START_DATE));
            long endTime = c.getLong(c.getColumnIndex(RECORD_END_DATE));
            String surveyResponse = c.getString(c.getColumnIndex(RECORD_SURVEY_RESPONSE));
            boolean surveyUploaded = c.getInt(c.getColumnIndex(RECORD_SURVEY_UPLOADED)) > 0;

            record.setID(id);
            record.setStartDate(new Date(startDate));
            record.setEndDate(new Date(endTime));
            record.setSurveyResponse(surveyResponse);
            record.setSurveyUploaded(surveyUploaded);
        }
        return record;
    }

    /**
     * DESCRIPTION:
     * Convenience method to convert a TripRecord instance to a set of key/value
     * pairs in a ContentValues instance utilized by SQLite access methods.
     *
     * @param record - the GasRecord to convert.
     * @return a ContentValues instance representing the specified GasRecord.
     */
    private ContentValues getContentValues(TripRecord record) {
        ContentValues values = new ContentValues();
        values.put(RECORD_ID, record.getID());
        values.put(RECORD_START_DATE, record.getStartDate().getTime());
        if (record.getEndDate() != null)
            values.put(RECORD_END_DATE, record.getEndDate().getTime());
        values.put(RECORD_SURVEY_RESPONSE, record.getSurveyResponse());
        values.put(RECORD_SURVEY_UPLOADED, record.getSurveyUploaded());
        return values;
    }
}
