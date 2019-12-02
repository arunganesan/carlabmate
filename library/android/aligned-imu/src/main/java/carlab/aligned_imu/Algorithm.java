package carlab.aligned_imu;

import android.content.Context;
import android.location.Location;
import android.renderscript.Float2;
import android.renderscript.Float3;

import edu.umich.carlab.CLDataProvider;

public class Algorithm extends AlgorithmBase {
    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    private Float3 lastGps = null;

    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public Float[] produceWorldPointingRotation (Float3 m, Float3 g) {
        // Cross product of magnet and gravity
        return rotmat(m, g);
    }


    @Override
    public Float3 produceWorldAlignedGyro (Float3 gyro, Float[] rm) {
        return mmul(gyro, rm);
    }

    @Override
    public Float3 produceWorldAlignedAccel (Float3 accel, Float[] rm) {
        return mmul(accel, rm);
    }

    @Override
    public Float[] produceVehiclePointingRotation (Float3 magnet, Float3 gps, Float3 gravity) {
        if (lastGps == null) {
            lastGps = gps;
            return null;
        }

        Float2 delta = norm(new Float2(gps.x - lastGps.x, gps.y - lastGps.y));
        Float3 bearingInWorldFrame = new Float3(delta.x, delta.y, 0);
        Float[] transformationMatrix = rotmat(magnet, gravity);
        Float3 bearingInPhoneFrame = mmul(transformationMatrix, bearingInWorldFrame);
        return rotmat(gravity, bearingInPhoneFrame);
    }

    @Override
    public Float3 produceVehicleAlignedAccel(Float3 accel, Float[] rm) {
        return mmul(accel, rm);
    }

    @Override
    public Float produceGravityAlignedGyro(Float3 gravity, Float3 gyro) {
        return gravity.x*gyro.x + gravity.y*gyro.y + gravity.z*gyro.z;
    }


    Float[] rotmat (Float3 m, Float3 g) {
        Float[] rm = new Float[9];


        Float3 c = new Float3(
                m.y * g.z - m.z * g.y,
                m.z * g.x - m.x * g.z,
                m.x * g.y - m.y * g.x);

        // Divide by norm
        c = norm(c);
        g = norm(g);

        // Reconstruct the magnet using another cross product
        Float3 nm = new Float3(
                g.y * c.z - g.z * c.y,
                g.z * c.x - g.x * c.z,
                g.x * c.y - g.y * c.x);

        // Assign values
        // First row
        rm[0] = c.x;
        rm[1] = c.y;
        rm[2] = c.z;

        // Second row
        rm[3] = nm.x;
        rm[3 + 1] = nm.y;
        rm[3 + 2] = nm.z;

        // Third row
        rm[2 * 3] = g.x;
        rm[2 * 3 + 1] = g.y;
        rm[2 * 3 + 2] = g.z;

        return rm;
    }


    Float2 norm (Float2 vec) {
        Float2 c = new Float2(vec.x, vec.y);
        float norm = (float) Math.sqrt(c.x * c.x + c.y * c.y);
        c.x /= norm;
        c.y /= norm;
        return c;
    }

    Float3 norm (Float3 vec) {
        Float3 c = new Float3(vec.x, vec.y, vec.z);
        float norm = (float) Math.sqrt(c.x * c.x + c.y * c.y + c.z * c.z);
        c.x /= norm;
        c.y /= norm;
        c.z /= norm;
        return c;
    }



    public Float3 mmul (Float[] mat, Float3 v) {
        return new Float3(
        mat[0] * v.x + mat[1] * v.y + mat[2] * v.z,
        mat[3] * v.x + mat[4] * v.y + mat[5] * v.z,
        mat[6] * v.x + mat[7] * v.y + mat[8] * v.z
        );
    }

    public Float3 mmul (Float3 T3, Float[] RotMat) {
        Float[] T = new Float[] { T3.x, T3.y, T3.z };
        Float[] temp = T.clone();
        for (int i = 0; i < T.length; i++)
            for (int j = 0; j < T.length; j++) {
                temp[i] = temp[i] + T[j] * RotMat[j * 3 + i];
            }
        return new Float3(temp[0], temp[1], temp[2]);
    }
}
