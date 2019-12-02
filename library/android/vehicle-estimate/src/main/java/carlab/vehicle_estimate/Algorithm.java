package carlab.vehicle_estimate;

import android.content.Context;
import android.renderscript.Float3;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.Registry;

public class Algorithm extends AlgorithmBase {
    final double INCHES_TO_METERS = 0.0254;
    final float MPS_TO_KMPH = 1 / 0.621371f;
    final float alpha = 0.3f;
    float STEERING_RATIO = 14.8f;
    String TAG = "algorithm";
    float VEHICLE_LENGTH = (float) (193.9 * INCHES_TO_METERS);
    float[][] inputBuffer = new float[5][1];
    float[][] labelProb = new float[1][10];
    float lastMeasuredSpeed = 0;
    Float lastSpeed = null;
    long lastSpeedAtTime = 0;
    Float lastYaw = null;
    Object runningPredictionLock = new Object();

    Interpreter tflite;

    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
        enableHistoricalLogging = true;
    }

    private static MappedByteBuffer loadModelFile (String modelFilename) throws IOException {
        FileInputStream inputStream = new FileInputStream(modelFilename);
        FileChannel fileChannel = inputStream.getChannel();
        long declaredLength = fileChannel.size();
        long startOffset = fileChannel.position();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public Integer estimateGear (Float CarSpeed, String GearModelFile) {
        // 1. Check if the model is loaded. If not, then download the model
        if (tflite == null) {
            Log.v(TAG, "Loading tensorflow model");
            try {
                tflite = new Interpreter(loadModelFile(GearModelFile), null);
            } catch (Exception e) {
                Log.e(TAG, "Error when loading tensorflow file");
                e.printStackTrace();
                return null;
            }
        }

        synchronized (runningPredictionLock) {
            // 2. Get the speed samples at the last [-4, -3, -2, -1, 0] seconds (similar to getLatestData(dev, sen))
            DataSample f1 = getDataAt(Registry.CarSpeed.name, 4000L);
            DataSample f2 = getDataAt(Registry.CarSpeed.name, 3000L);
            DataSample f3 = getDataAt(Registry.CarSpeed.name, 2000L);
            DataSample f4 = getDataAt(Registry.CarSpeed.name, 1000L);
            DataSample f5 = getDataAt(Registry.CarSpeed.name, 0L);

            if (f1 == null || f2 == null || f3 == null || f4 == null || f5 == null) {
                Log.e(TAG, String.format("Couldn't construct feature vector [%d, %d, %d, %d, %d]",
                                         (f1 == null) ? 0 : 1, (f2 == null) ? 0 : 1,
                                         (f3 == null) ? 0 : 1, (f4 == null) ? 0 : 1,
                                         (f5 == null) ? 0 : 1));
                return null;
            }

            inputBuffer[0][0] = f1.value;
            inputBuffer[1][0] = f2.value;
            inputBuffer[2][0] = f3.value;
            inputBuffer[3][0] = f4.value;
            inputBuffer[4][0] = f5.value;

            // 3. Use feature set to make prediction
            tflite.run(inputBuffer, labelProb);

            // 4. Use reverse one-hot encoding to output the gear value
            float gearValue = oneHotDecode(labelProb);
            Log.v(TAG,
                  String.format("[%.02f, %.02f, %.02f, %.02f, %.02f] -> %d", f1.value, f2.value,
                                f3.value, f4.value, f5.value, (int) gearValue));

            return (int) gearValue;
        }
    }

    @Override
    public Float estimateSpeed (Float3 VehicleAlignedAccel, Float3 GPS, String CarModel) {
        // Basic equation: v = a * (v + acc * dt) + (1-a) * gps_speed
        float gSpeed = GPS.z * MPS_TO_KMPH;

        if (lastSpeedAtTime == 0) {
            lastMeasuredSpeed = gSpeed;
        } else {
            long dt = System.currentTimeMillis() - lastSpeedAtTime;
            lastMeasuredSpeed =
                    alpha * (lastMeasuredSpeed + VehicleAlignedAccel.x * ((float) dt / 1000.0f)) +
                    (1 - alpha) * gSpeed;
        }

        lastSpeedAtTime = System.currentTimeMillis();
        return lastMeasuredSpeed;
    }

    @Override
    public Float estimateSteering (Float CarSpeed, Float GravityAlignedGyro, String CarModel) {
        Double steering = (double) (lastSpeed / lastYaw);
        steering = Math.asin(VEHICLE_LENGTH / steering);
        steering = STEERING_RATIO * steering;
        steering *= 180 / Math.PI;
        return steering.floatValue();
    }

    Integer oneHotDecode (float[][] labelProb) {
        float maxVal = labelProb[0][0];
        int maxIdx = 0;
        float val;

        for (int i = 1; i < labelProb[0].length; i++) {
            val = labelProb[0][i];
            if (val > maxVal) {
                maxVal = val;
                maxIdx = i;
            }
        }

        // One hot encoding basically shifts the gear values over.
        return maxIdx - 1;
    }
}
