package carlab.aligned_imu;

import android.content.Context;
import android.renderscript.Float3;

import edu.umich.carlab.CLDataProvider;

public class Algorithm extends AlgorithmBase {
    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public Float[] produceWorldPointingRotation (Float3 m, Float3 g) {
        // Cross product of magnet and gravity
        Float[] rm = new Float[9];


        Float3 c = new Float3(
                m.y * g.z - m.z * g.y,
                m.z * g.x - m.x * g.z,
                m.x * g.y - m.y * g.x);

        // Divide by norm
        float norm = (float) Math.sqrt(c.x * c.x + c.y * c.y + c.z * c.z);
        c.x /= norm;
        c.y /= norm;
        c.z /= norm;
        norm = (float) Math.sqrt(g.x * g.x + g.y * g.y + g.z * g.z);
        g.x /= norm;
        g.y /= norm;
        g.z /= norm;

        // Reconstruct the magnet using another cross product
        Float3 nm = new Float3(
                g.y * c.z - g.z * c.y,
                g.z * c.x - g.x * c.z,
                g.x * c.y - g.y * c.x);

        // Assign values
        rm[0] = c.x;
        rm[1] = c.y;
        rm[2] = c.z;
        rm[3] = nm.x;
        rm[3 + 1] = nm.y;
        rm[3 + 2] = nm.z;
        rm[2 * 3] = g.x;
        rm[2 * 3 + 1] = g.y;
        rm[2 * 3 + 2] = g.z;

        return rm;
    }


    @Override
    public Float3 produceWorldAlignedGyro (Float3 gyro, Float[] rm) {
        return MatrixMul(gyro, rm);
    }

    @Override
    public Float3 produceWorldAlignedAccel (Float3 accel, Float[] rm) {
        return MatrixMul(accel, rm);
    }

    @Override
    public Float[] produceVehiclePointingRotation (Float3 magnet, Float3 gps, Float3 gravity) {
        // Take consecutive GPS points, get the bearing
        // Get basis vector based on that and magnetic north
        // Translate back
        // TODO indeed.
        return new Float[]{};
    }

    @Override
    public Float3 produceVehicleAlignedAccel(Float3 accel, Float[] rm) {
        return MatrixMul(accel, rm);
    }

    @Override
    public Float produceGravityAlignedGyro(Float3 gravity, Float3 gyro) {
        // AKA the dot product
        return gravity.x*gyro.x + gravity.y*gyro.y + gravity.z*gyro.z;
    }


    public Float3 MatrixMul (Float3 T3, Float[] RotMat) {
        Float[] T = new Float[] { T3.x, T3.y, T3.z };
        Float[] temp = T.clone();
        for (int i = 0; i < T.length; i++)
            for (int j = 0; j < T.length; j++) {
                temp[i] = temp[i] + T[j] * RotMat[j * 3 + i];
            }
        return new Float3(temp[0], temp[1], temp[2]);
    }
}
