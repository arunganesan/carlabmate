package edu.umich.carlab;

import android.app.Activity;

/**
 * Created by arunganesan on 1/21/18.
 */

public class Constants {
    public static final String INTENT_APP_STATE_UPDATE = "edu.umich.carlab.APP_STATE_UPDATE";
    public static final String BLUETOOTH_CONN_FAILED = "BluetoothConnFailed";

    public static final int TAG_CODE_PERMISSION_LOCATION = 777;


    public final static int GPS_INTERVAL = 500;

    // Sample OXC at 10 Hz.
    public final static int OXC_PERIOD = 100;

    /** Upload URL */
    public static final String BASE_URL = "http://barca.eecs.umich.edu:9000/";
    public static final String DEFAULT_UPLOAD_URL = BASE_URL + "/upload";
    public static final String LIST_UPLOADED_URL = BASE_URL + "/uploaded";
    public static final String LATEST_TRIP_URL = BASE_URL + "/latestTrip";

    public static final String GET_LATEST_LOG_URL = BASE_URL + "/clog/latest";
    public static final String UPLOAD_LOG_URL = BASE_URL + "/clog/upload";

    public static final String VERSION_URL = BASE_URL + "/experiment/version";
    public static final int CARLAB_NOTIFICATION_ID = 0x818;
    public static long RemainingDataCount = 0L;


    /* Display parameters */
    public static final String Notification_Channel = "carlab_channel_01";

    /* Shared preferences keys */
    public static final String SELECTED_BLUETOOTH_KEY = "Bluetooth Selected Device";
    public static final String BLACKLIST_APPS_KEY = "blacklist apps keys";
    public static String UID_key = "uid";
    public static final String Main_Activity = "this main activity";

    public static final String LIVE_MODE = "live mode - no saving";


    public static final String Session_State_Key = "session state key";
    public static final String Dump_Data_Mode_Key = "dump data mode activated";
    public static final String Load_From_Trace_Key = "load from this trace file";
    public static final String Load_From_Trace_Duration_Start = "load from this trace file starting from";
    public static final String Load_From_Trace_Duration_End = "load from this trace file ending at";
    public static final String Load_Attack_From_Specs_Key = "load attack from specs key";
    public static final int wakeupCheckPeriod = 30*1000; //10*1000;
    public static final int sleepCheckPeriod = 5*1000; //5*1000;

    public static final String ManualChoiceKey = "Manual Button State";
;

    public static final String Static_Apps = "static apps";
    public static final String Middlewares = "middlewares";

    public static final String Trip_Id_Offset = "trip id offset";

    // Experiment details
    public static final String Experiment_Id = "experiment id";
    public static final String Experiment_Shortname = "experiment shortname";
    public static final String Experiment_Version_Number = "experiment version";
    public static final String Experiment_New_Version_Detected = "experiment update needed";
    public static final String Experiment_New_Version_Last_Checked = "experiment last checked";


    /** Return value codes **/
    public final static float NO_GPS_PERMISSION_ERROR = -2;
    public final static float GENERAL_ERROR = -3;
    public final static float GPS_STARTED_STATUS = 2;
    public final static float STARTING_STATUS = 3;
    public final static float GENERAL_STATUS = 4;

    /* Intents flying around */
    public static final String MASTER_SWITCH_ON = "MasterSwitchON";
    public static final String MASTER_SWITCH_OFF = "MasterSwitchOFF";
    public static final String DONE_INITIALIZING_CL = "CanEnableMSNow";
    public static final String TRIGGER_BT_SEARCH = "TriggerBTSearch";
    public static final String CLSERVICE_STOPPED = "CLSERVICE_STOPPED";
    public static final String STATUS_CHANGED = "StatusChanged";
    public static final String BT_FAILED = "bluetooth failed";

    public static final String REPLAY_STATUS = "ReplayStatus";
    public static final String DUMP_COLLECTED_STATUS = "DumpCollectedStatus";
    public static final String REPLAY_PERCENTAGE = "ReplayPercentage";
    public static final String DUMP_BYTES = "DumpBytes";


    public static final String DONE_RUNNING_SPEC_FILE = "DoneRunningSpecFile";
    public static final String SPEC_RESULT_FILENAME = "SpecResultFilename";

    public static final String TASKS_APK_NAME = "tasks.apk";

}
