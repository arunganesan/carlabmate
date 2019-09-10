package edu.umich.carlab.net;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import java.util.Map;


/**
 * Keep internal database or store in files.
 * Periodically wake up if there is data to upload it
 * See UploadFiles code.
 */
public class PacketHandleService extends Service {
    public final String TAG = PacketHandleService.class.getName();
    final IBinder mBinder = new PacketHandleService.LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        // Or an intent
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }


    /****************************************************************
     * Public functions
     ***************************************************************/
    public boolean outputNewInfo (String info, Object data) {
        /**
         * Add the data to the internal database
         * Will be uploaded asynchronously
         */
        return true;
    }

    public Map<String, Object> checkNewInfo () {
        /**
         * If there data in the local database, then return those pairs
         * This is a non-blocking call
         */
        return null;
    }

    public class LocalBinder extends Binder {
        public PacketHandleService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PacketHandleService.this;
        }
    }
}


