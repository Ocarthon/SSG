package de.ocarthon.ssg.math;

import java.util.Comparator;
import java.util.List;

public class Vector {
    public double x;
    public double y;
    public double z;

    public static final Vector X = new Vector(1, 0, 0);
    public static final Vector Y = new Vector(0, 1, 0);
    public static final Vector Z = new Vector(0, 0, 1);

    public Vector() {
    }

    public Vector(double x, double y, double z) {
        set(x, y, z);
    }

    public Vector copy() {
        return new Vector(x, y, z);
    }

    public Vector set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector set(Vector v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    public Vector add(double x, double y, double z) {
        return set(this.x + x, this.y + y, this.z + z);
    }

    public Vector add(Vector vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public Vector sub(double x, double y, double z) {
        return set(this.x - x, this.y - y, this.z - z);
    }

    public Vector sub(Vector vec) {
        return sub(vec.x, vec.y, vec.z);
    }

    public Vector mult(double scalar) {
        return set(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double dot(Vector vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public Vector setMag(double mag) {
        return this.mult(mag / length());
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthXY() {
        return Math.sqrt(x * x + y * y);
    }

    public double length2() {
        return x * x + y * y + z * z;
    }

    public Vector norm() {
        double l = length();
        x /= l;
        y /= l;
        z /= l;

        return this;
    }

    /**
     * xn = y*a.z-z*a.y
     * yn = z*a.x-x*a.z
     * zn = x*a.y-y*a.x
     */
    public static Vector cross(Vector v1, Vector v2) {
        return new Vector(v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x);
    }

    public static double angle(Vector v1, Vector v2) {
        double a = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
        double b = v1.length() * v2.length();
        return Math.acos(a / b);
    }

    public static double dst2(Vector v1, Vector v2) {
        return Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2) + Math.pow(v1.z - v2.z, 2);
    }

    public static double dst2XY(Vector v1, Vector v2) {
        return Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2);
    }

    public static double dst(Vector v1, Vector v2) {
        return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2) + Math.pow(v1.z - v2.z, 2));
    }

    public static Vector add(Vector v1, Vector v2) {
        return new Vector(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static Vector sub(Vector v1, Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public static void sortByPolarAngle(List<Vector> vectors, Vector origin) {
        vectors.sort(new PolarVecComp(origin));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        return MathUtil.equals(vector.x, x) && MathUtil.equals(vector.y, y) && MathUtil.equals(vector.z, z);
    }

    public boolean equals(Object o, double eps) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        return MathUtil.equals(vector.x, x, eps) && MathUtil.equals(vector.y, y, eps) && MathUtil.equals(vector.z, z, eps);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public static class PolarVecComp implements Comparator<Vector> {
        private Vector ref;

        public PolarVecComp(Vector ref) {
            this.ref = ref;
        }

        @Override
        public int compare(Vector o1, Vector o2) {
            Vector r1 = o1.copy().sub(ref);
            Vector r2 = o2.copy().sub(ref);
            double at1 = Math.atan2(r1.y, r1.x);
            double at2 = Math.atan2(r2.y, r2.x);

            if (at1 < at2) return -1;
            if (at1 > at2) return 1;
            return 0;
        }
    }
}
