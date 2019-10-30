package edu.umich.aligned_imu;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;

public class AlignedIMU extends Algorithm {
    public AlignedIMU(CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public float[][] produceRotation (float [] m, float [] g) {
        // Cross product of magnet and gravity
        float[][] rm = new float[3][3];

        float[] c = {
                m[1]*g[2]-m[2]*g[1],
                m[2]*g[0]-m[0]*g[2],
                m[0]*g[1]-m[1]*g[0]
        };

        // Divide by norm
        float norm = (float)Math.sqrt(c[0]*c[0] + c[1]*c[1] + c[2]*c[2]);
        c[0] /= norm; c[1] /= norm; c[2] /= norm;
        norm = (float)Math.sqrt(g[0]*g[0] + g[1]*g[1] + g[2]*g[2]);
        g[0] /= norm; g[1] /= norm; g[2] /= norm;

        // Reconstruct the magnet using another cross product
        float[] nm = {
                g[1]*c[2]-g[2]*c[1],
                g[2]*c[0]-g[0]*c[2],
                g[0]*c[1]-g[1]*c[0]
        };

        // Assign values
        rm[0][0] = c[0];    rm[0][1] = c[1];    rm[0][2] = c[2];
        rm[1][0] = nm[0];   rm[1][1] = nm[1];   rm[1][2] = nm[2];
        rm[2][0] = g[0];    rm[2][1] = g[1];    rm[2][2] = g[2];

        return rm;
    }

    @Override
    public float[] produceAlignedGyro (float [] gyro, float [][] rm) {
        return MatrixMul(gyro, rm);
    }

    @Override
    public float[] produceAlignedAccel (float [] accel, float [] [] rm) {
        return MatrixMul(accel, rm);
    }

    public float[] MatrixMul(float[] T, float[][] RotMat) {
        float[] temp = T.clone();
        for (int i = 0; i < RotMat.length; i++)
            for (int j = 0; j < T.length; j++) {
                temp[i] = temp[i] + T[j] * RotMat[j][i];
            }
        return temp;
    }
}
