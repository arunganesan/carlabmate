package carlab.aligned_imu;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;

public class Algorithm extends AlgorithmBase {
    public Algorithm (CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public Float[] produceRotation (Float[] m, Float[] g) {
        // Cross product of magnet and gravity
        Float[] rm = new Float[9];

        float[] c =
                {m[1] * g[2] - m[2] * g[1], m[2] * g[0] - m[0] * g[2], m[0] * g[1] - m[1] * g[0]};

        // Divide by norm
        float norm = (float) Math.sqrt(c[0] * c[0] + c[1] * c[1] + c[2] * c[2]);
        c[0] /= norm;
        c[1] /= norm;
        c[2] /= norm;
        norm = (float) Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);
        g[0] /= norm;
        g[1] /= norm;
        g[2] /= norm;

        // Reconstruct the magnet using another cross product
        float[] nm =
                {g[1] * c[2] - g[2] * c[1], g[2] * c[0] - g[0] * c[2], g[0] * c[1] - g[1] * c[0]};

        // Assign values
        rm[0] = c[0];
        rm[1] = c[1];
        rm[2] = c[2];
        rm[3] = nm[0];
        rm[3 + 1] = nm[1];
        rm[3 + 2] = nm[2];
        rm[2 * 3] = g[0];
        rm[2 * 3 + 1] = g[1];
        rm[2 * 3 + 2] = g[2];

        return rm;
    }

    @Override
    public Float[] produceAlignedGyro (Float[] gyro, Float[] rm) {
        return MatrixMul(gyro, rm);
    }

    @Override
    public Float[] produceAlignedAccel (Float[] accel, Float[] rm) {
        return MatrixMul(accel, rm);
    }

    public Float[] MatrixMul (Float[] T, Float[] RotMat) {
        Float[] temp = T.clone();
        for (int i = 0; i < T.length; i++)
            for (int j = 0; j < T.length; j++) {
                temp[i] = temp[i] + T[j] * RotMat[j * 3 + i];
            }
        return temp;
    }
}
