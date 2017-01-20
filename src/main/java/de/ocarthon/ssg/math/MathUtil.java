package de.ocarthon.ssg.math;

public class MathUtil {

    private MathUtil() {
    }

    public static double findLowestPoint(Facet f) {
        double l = f.p1.z;
        if (f.p2.z < l) l = f.p2.z;
        if (f.p3.z < l) l = f.p3.z;

        return l;
    }

    public static Vector barycentricCoordinates(Facet f, Vector p) {
        double dom = (f.p1.y - f.p3.y) * (f.p2.x - f.p3.x) + (f.p2.y - f.p3.y) * (f.p3.x - f.p1.x);

        if (dom != 0) {
            Vector b = new Vector();

            b.x = ((p.y - f.p3.y) * (f.p2.x - f.p3.x) + (f.p2.y - f.p3.y) * (f.p3.x - p.x)) / dom;
            b.y = ((p.y - f.p1.y) * (f.p3.x - f.p1.x) + (f.p3.y - f.p1.y) * (f.p1.x - p.x)) / dom;
            b.z = ((p.y - f.p2.y) * (f.p1.x - f.p2.x) + (f.p1.y - f.p2.y) * (f.p2.x - p.x)) / dom;

            return b;
        } else {
            return new Vector(-1, -1, -1);
        }
    }
}
