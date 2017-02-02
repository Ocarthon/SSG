package de.ocarthon.ssg.math;

public class MathUtil {
    private static final double DOUBLE_EPS = 0.0000001;
    private MathUtil() {
    }

    public static double findLowestPoint(Facet f) {
        double l = f.p1.z;
        if (f.p2.z < l) l = f.p2.z;
        if (f.p3.z < l) l = f.p3.z;

        return l;
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < DOUBLE_EPS;
    }
}
