package de.ocarthon.ssg.math;

import java.util.List;

public class Centroid {

    public static Vector calculateCentroid(List<Vector> vectors) {
        double cx = 0;
        double cy = 0;
        double a = 0;


        Vector v;
        Vector v1;

        for (int i = 0; i < vectors.size() - 1; i++) {
            v = vectors.get(i);
            v1 = vectors.get(i + 1);
            double t = (v.x * v1.y - v1.x * v.y);
            cx += (v.x+v1.x) * t;
            cy += (v.y+v1.y) * t;

            a += t;
        }

        a *= 3;

        return new Vector(cx / a, cy / a, 0);
    }
}
