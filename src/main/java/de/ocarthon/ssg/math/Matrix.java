package de.ocarthon.ssg.math;


import java.util.Arrays;

public class Matrix {
    public double[] v = new double[9];
    public Matrix(double v11, double v12, double v13, double v21, double v22, double v23,
                  double v31, double v32, double v33) {
        v[0] = v11;
        v[1] = v12;
        v[2] = v13;
        v[3] = v21;
        v[4] = v22;
        v[5] = v23;
        v[6] = v31;
        v[7] = v32;
        v[8] = v33;
    }

    private Matrix(double[] v) {
        this.v = v;
    }

    public Matrix multiply(Matrix m) {
        double[] mv = new double[9];

        mv[0] = v[0] * m.v[0] + v[1] * m.v[3] + v[2] * m.v[6]; // row = 0; col = 0
        mv[1] = v[0] * m.v[1] + v[1] * m.v[4] + v[2] * m.v[7]; // row = 0; col = 1
        mv[2] = v[0] * m.v[2] + v[1] * m.v[5] + v[2] * m.v[8]; // row = 0; col = 2
        mv[3] = v[3] * m.v[0] + v[4] * m.v[3] + v[5] * m.v[6]; // row = 1; col = 0
        mv[4] = v[3] * m.v[1] + v[4] * m.v[4] + v[5] * m.v[7]; // row = 1; col = 1
        mv[5] = v[3] * m.v[2] + v[4] * m.v[5] + v[5] * m.v[8]; // row = 1; col = 2
        mv[6] = v[6] * m.v[0] + v[7] * m.v[3] + v[8] * m.v[6]; // row = 2; col = 0
        mv[7] = v[6] * m.v[1] + v[7] * m.v[4] + v[8] * m.v[7]; // row = 2; col = 1
        mv[8] = v[6] * m.v[2] + v[7] * m.v[5] + v[8] * m.v[8]; // row = 2; col = 2

        return new Matrix(mv);
    }

    public Vector transform(Vector in) {
        Vector out = new Vector();
        out.x = in.x*v[0]+in.y*v[1]+in.z*v[2];
        out.y = in.x*v[3]+in.y*v[4]+in.z*v[5];
        out.z = in.x*v[6]+in.y*v[7]+in.z*v[8];

        return out;
    }

    public Facet transform(Facet in) {
        return new Facet(transform(in.p1), transform(in.p2), transform(in.p3));
    }

    public static Matrix rotationMatrix(double xAxis, double yAxis, double zAxis) {
        double sinX = Math.sin(xAxis);
        double cosX = Math.cos(xAxis);

        double sinY = Math.sin(yAxis);
        double cosY = Math.cos(yAxis);

        double sinZ = Math.sin(zAxis);
        double cosZ = Math.cos(zAxis);

        double[] v = new double[9];

        v[0] = cosY * cosZ;
        v[1] = -cosY * sinZ;
        v[2] = sinY;
        v[3] = cosZ * sinX * sinY + cosX * sinZ;
        v[4] = cosX * cosZ - sinX * sinY * sinZ;
        v[5] = -cosY * sinX;
        v[6] = sinX * sinZ - cosX * cosZ * sinY;
        v[7] = cosZ * sinX + cosX * sinY * sinZ;
        v[8] = cosX * cosY;

        return new Matrix(v);
    }

    public static Matrix scaleMatrix(double factor) {
        return new Matrix(factor, 0, 0, 0, factor, 0, 0, 0, factor);
    }

    @Override
    public String toString() {
        return "Matrix{" + Arrays.toString(v) + '}';
    }
}
