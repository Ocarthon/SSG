package de.ocarthon.ssg.math;

public class Vector {
    public double x;
    public double y;
    public double z;

    public static final Vector X = new Vector(1, 0, 0);
    public static final Vector Y = new Vector(0, 1, 0);
    public static final Vector Z = new Vector(0, 0, 1);
    public static final Vector ZERO = new Vector(0, 0, 0);

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

    public Vector scale(double scalar) {
        return set(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double length() {
        return Math.sqrt(x*x+y*y+z*z);
    }

    public double lengthXY() {
        return Math.sqrt(x*x+y*y);
    }

    public double length2() {
        return x*x+y*y+z*z;
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

    public static double dst(Vector v1, Vector v2) {
        return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2) + Math.pow(v1.z - v2.z, 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        return Double.compare(vector.x, x) == 0 && Double.compare(vector.y, y) == 0 && Double.compare(vector.z, z) == 0;

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
}
