package edu.umich.carlab.net;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.util.Log;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.protocol.*;
import com.github.pires.obd.enums.ObdProtocols;
import edu.umich.carlab.Constants;
import edu.umich.carlab.hal.controllers.ObdConfig;
import edu.umich.carlab.io.ObdCommandJob;
import edu.umich.carlab.sensors.ObdSensors;
import edu.umich.carlab.utils.NotificationsHelper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by arunganesan on 4/2/18.config
 */

public class OBDConnTimerTask extends TimerTask {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final String TAG = BluetoothConnService.class.getName();
    public BluetoothSocket socket;
    BluetoothConnService service;
    SharedPreferences prefs;

    public OBDConnTimerTask(BluetoothConnService service, SharedPreferences prefs) {
        this.service = service;
        this.prefs = prefs;
    }


    String flushInput() {
        byte b = 0;
        StringBuilder res = new StringBuilder();
        char c;
        try {
            while (((b = (byte) socket.getInputStream().read()) > -1)) {
                c = (char) b;
                if (c == '>') // read until '>' arrives
                {
                    break;
                }
                res.append(c);
            }

        } catch (IOException e) {
            Log.v(TAG, "OK... fine read returned -1 as expected.");
        }
        return res.toString();
    }


    Float runCommandSync(ObdCommand obdCommand) throws Exception {
        ObdCommandJob job = new ObdCommandJob(obdCommand);
        job.getCommand().run(socket.getInputStream(), socket.getOutputStream());
        float responseVal = ObdSensors.responseToFloat(job.getCommand());
        return responseVal;
    }


    @Override
    public void run() {

        Log.v(TAG, "Trying to connect.");
        if (service.isActive() &&
                service.lastSuccessfulConnection > (System.currentTimeMillis() - service.MINUTE)) return;

        // Else service isn't active or we haven't verified the service activity in the last 1 minute
        service.connectionFailed();

        NotificationsHelper.setNotificationForeground(service, NotificationsHelper.Notifications.DISCOVERY);

        final String remoteDevice = prefs.getString(Constants.SELECTED_BLUETOOTH_KEY, null);

        if (remoteDevice == null || "".equals(remoteDevice)) {
            Log.e(TAG, "No Bluetooth device has been selected.");
            NotificationsHelper.setNotificationForeground(service, NotificationsHelper.Notifications.DISCOVERY_FAIL);
        } else {
            int attempts = 2;
            while (attempts-- > 0) {
                Log.e(TAG, "Attempt #" + attempts);
                NotificationsHelper.setNotificationForeground(service, NotificationsHelper.Notifications.DISCOVERY);
                try {
                    final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dev = btAdapter.getRemoteDevice(remoteDevice);
                    Log.d(TAG, "Stopping Bluetooth discovery.");
                    btAdapter.cancelDiscovery();

                    socket = connect(dev);
                    if (socket == null) throw new Exception("Connect failed");

                    Log.e(TAG, "Connected to BT. Performing initialization sequence");
                    Thread.sleep(1500);


                    // TODO This should be in shared prefs. Ideally we have code that isn't OBD-device specific
//                    if (myApp.collectionType == MyApplication.DataCollectionType.OBD1) {
                    String flushed = flushInput();
                    Thread.sleep(1500);
                    flushed = flushInput();
                    Thread.sleep(1500);
//                    }

                    Log.d(TAG, "Queueing jobs for connection configuration..");
                    runCommandSync(new ObdResetCommand());

                    Thread.sleep(1500);
                    runCommandSync(new EchoOffCommand());

                    runCommandSync(new EchoOffCommand());
                    runCommandSync(new LineFeedOffCommand());
                    runCommandSync(new TimeoutCommand(62));

                    // Get protocol from preferences
                    // XXX If we already know the protocol, we can/should save it in the shared preferences
                    final String protocol = "AUTO";
                    runCommandSync(new SelectProtocolCommand(ObdProtocols.valueOf(protocol)));

                    // Use the results to update list of valid sensors
                    ObdConfig.addAllDefault();
                    service.connectionSucceeded(socket);
                    NotificationsHelper.setNotificationForeground(service, NotificationsHelper.Notifications.STARTING);
                    break;
                } catch (Exception e) {
                    Log.e(
                            TAG,
                            "There was an error while establishing connection. -> "
                                    + e.getMessage()
                    );

                    NotificationsHelper.setNotificationForeground(service, NotificationsHelper.Notifications.DISCOVERY_FAIL);
                }
            }

            // We reach here means after trying 5 times, the connection is still not active
            if (!service.isActive()) {
                service.unableToConnect();
            }
        }
    }


    /**
     * Instantiates a BluetoothSocket for the remote device and connects it.
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @param dev The remote device to connect to
     * @return The BluetoothSocket
     * @throws IOException
     */
    public BluetoothSocket connect(BluetoothDevice dev) throws IOException {
        BluetoothSocket sock = null;
        BluetoothSocket sockFallback = null;

        Log.d(TAG, "Starting Bluetooth connection..");
        try {
            sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
            //try { Thread.sleep(500); } catch (Exception e) {};
            sock.connect();
        } catch (Exception e1) {
            Log.e(TAG, "There was an error while establishing Bluetooth connection. Falling back", e1);
            Class<?> clazz = sock.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                sockFallback.connect();
                sock = sockFallback;
            } catch (Exception e2) {
                String errorMsg = "Couldn't fallback while establishing Bluetooth connection.";
                Log.e(TAG, errorMsg, e2);
                throw new IOException(e2.getMessage());
            }
        }

        return sock;
    }
}
