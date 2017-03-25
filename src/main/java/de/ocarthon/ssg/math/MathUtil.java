package de.ocarthon.ssg.math;


public final class MathUtil {
    public static boolean HIGH_TOLERANCE = true;
    private static double DOUBLE_EPS_HIGH = 0.1;
    private static double DOUBLE_EPS_LOW = 0.0000001;

    private MathUtil() {
    }

    public static double round(double num, int pres) {
        return Math.round(Math.pow(10, pres) * num) / Math.pow(10, pres);
    }

    /**
     * calculates the distance between a point and a line
     *
     * @param p  point
     * @param p1 first point on the line
     * @param p2 second point on the line
     * @return distance between point p and line
     */
    public static double dst2PointLine(Vector p, Vector p1, Vector p2) {
        Vector st = solveLSE(p2.x - p1.x, p1.y - p2.y, p.x - p1.x, p2.y - p1.y, p2.x - p1.x, p.y - p1.y);
        if (st == null) {
            return Double.NaN;
        }

        if (st.x >= 0 && st.x <= 1) {
            double t = st.y;
            return Math.pow(t, 2) * (Math.pow(p2.y - p1.y, 2) + Math.pow(p1.x - p2.x, 2));
        } else {
            return Double.NaN;
        }
    }

    public static double dst2PointTriangle(Vector p, Vector p1, Vector p2, Vector p3) {
        // Check if the point is inside the triangle
        Vector b = barycentricCoordinates(p, p1, p2, p3);
        if (b.x >= 0 && b.x <= 1 && b.y >= 0 && b.y <= 1 && b.z >= 0 && b.z <= 1) {
            return 0;
        }

        double min2 = Double.MAX_VALUE;

        min2 = Math.min(min2, Vector.dst2(p, p1));
        min2 = Math.min(min2, Vector.dst2(p, p2));
        min2 = Math.min(min2, Vector.dst2(p, p3));

        double a = dst2PointLine(p, p1, p2);
        if (a == a) {
            min2 = Math.min(min2, a);
        }

        a = dst2PointLine(p, p2, p3);
        if (a == a) {
            min2 = Math.min(min2, a);
        }

        a = dst2PointLine(p, p3, p1);
        if (a == a) {
            min2 = Math.min(min2, a);
        }

        return min2;
    }

    public static Vector barycentricCoordinates(Vector p, Vector p1, Vector p2, Vector p3) {
        double dom = (p1.y - p3.y) * (p2.x - p3.x) + (p2.y - p3.y) * (p3.x - p1.x);

        if (dom != 0) {
            Vector b = new Vector();

            b.x = ((p.y - p3.y) * (p2.x - p3.x) + (p2.y - p3.y) * (p3.x - p.x)) / dom;
            b.y = ((p.y - p1.y) * (p3.x - p1.x) + (p3.y - p1.y) * (p1.x - p.x)) / dom;
            b.z = ((p.y - p2.y) * (p1.x - p2.x) + (p1.y - p2.y) * (p2.x - p.x)) / dom;

            return b;
        } else {
            return new Vector(Double.NaN, Double.NaN, Double.NaN);
        }
    }

    public static boolean doesIntersect(Vector p0, Vector n, Vector a, Vector r) {
        double b = p0.dot(n);
        double t = (b - n.x * a.x - n.y * a.y - n.z * a.z) / (n.x * r.x + n.y * r.y + n.z * r.z);
        return t >= 0 && t <= 1;
    }

    // http://math.stackexchange.com/questions/7931/point-below-a-plane/7934#7934
    public static boolean isAbovePlane(Vector p, Vector p0, Vector n) {
        return n.dot(Vector.sub(p, p0)) > 0;
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < (HIGH_TOLERANCE ? DOUBLE_EPS_HIGH : DOUBLE_EPS_LOW);
    }

    public static boolean equals(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    public static Vector solveLSE(double a11, double a12, double b1, double a21, double a22, double b2) {
        try {
            double y = (a21 * b1 - a11 * b2) / (a12 * a21 - a11 * a22);

            double x;
            if (a11 == 0) {
                x = (b2 - y * a22) / (a21);
            } else {
                x = (b1 - y * a12) / (a11);
            }

            return new Vector(x, y, 0);
        } catch (Exception e) {
            return null;
        }
    }
}
