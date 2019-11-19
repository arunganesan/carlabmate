package edu.umich.carlab;

/**
 * Created by arunganesan on 1/21/18.
 */

public class Constants {
    public static final String BLUETOOTH_CONN_FAILED = "BluetoothConnFailed";
    public static final String BT_FAILED = "bluetooth failed";
    public static final int CARLAB_NOTIFICATION_ID = 0x818;
    public static final String CARLAB_STATUS = "CLSTATUS";
    public static final String Dump_Data_Mode_Key = "dump data mode activated";
    // Experiment details
    public static final String Experiment_Id = "experiment id";
    public static final String GATEWAY_STATUS = "GATEWAY";
    public final static float GENERAL_ERROR = -3;
    public final static float GENERAL_STATUS = 4;
    public final static int GPS_INTERVAL = 500;
    public final static float GPS_STARTED_STATUS = 2;
    public static final String INTENT_APP_STATE_UPDATE = "edu.umich.carlab.APP_STATE_UPDATE";
    public static final String LINKSERVER = "35.3.73.250";
    public static final String LIVE_MODE = "live mode - no saving";
    public static final String LOGINURL =
            "http://" + Constants.LINKSERVER + ":3000/login?username=%s&password=%s";
    public static final String Load_From_Trace_Duration_End = "load from this trace file ending at";
    public static final String Load_From_Trace_Duration_Start =
            "load from this trace file starting from";
    public static final String Load_From_Trace_Key = "load from this trace file";
    public static final String MASTER_SWITCH_OFF = "MasterSwitchOFF";
    /* Intents flying around */
    public static final String MASTER_SWITCH_ON = "MasterSwitchON";
    public static final String Main_Activity = "this main activity";
    public static final String ManualChoiceKey = "Manual Button State";
    /**
     * Return value codes
     **/
    public final static float NO_GPS_PERMISSION_ERROR = -2;
    /* Display parameters */
    public static final String Notification_Channel = "carlab_channel_01";
    // Sample OXC at 10 Hz.
    public final static int OXC_PERIOD = 100;
    public static final String REPLAY_PERCENTAGE = "ReplayPercentage";
    public static final String REPLAY_STATUS = "ReplayStatus";
    /* Shared preferences keys */
    public static final String SELECTED_BLUETOOTH_KEY = "Bluetooth Selected Device";
    public static final String SESSION = "session id";
    public final static float STARTING_STATUS = 3;
    public static final String Session_State_Key = "session state key";
    public static final String TRIGGER_BT_SEARCH = "TriggerBTSearch";
    public static final String Trip_Id_Offset = "trip id offset";
    public static final String UPLOAD_URL =
            "http://" + LINKSERVER + ":3000/packet/upload?information=%s&session=%s";
    public static final String _STATUS_MESSAGE = "MESSAGE";
    public static final int sleepCheckPeriod = 5 * 1000; //5*1000;
    public static final int wakeupCheckPeriod = 30 * 1000; //10*1000;
    public static long RemainingDataCount = 0L;
    public static String UID_key = "uid";

}
