package de.ocarthon.ssg.math;

import java.util.ArrayList;
import java.util.List;

public class Centroid {

    /**
     * Calculates the geometric center for a polygon with the given vectors
     * as vertices. The geometric center is the average of all vertices
     *
     * @param vectors vertices of a polygon
     * @return geometric center
     */
    public static Vector geometricCenter(List<Vector> vectors) {
        Vector center = new Vector(0, 0, 0);

        for (Vector v : vectors) {
            center.add(v);
        }

        center.mult(1d / vectors.size());
        return center;
    }

    /**
     * Calculates the chebychev center for a polygon with the given vectors
     * as vertices. The chebychev center is the deepest point in the polygon
     * in that the maximum distance between it and a vertex is minimal.
     * <p>
     * This algorithm is implemented as described in <a href="http://
     * www-m6.ma.tum.de/~turova/html/chebnew2.pdf">An Algorithm for Finding
     * the Chebychev Center of a Convex Polyhedron</a> by N.D.Botkin and
     * V.L.Turova-Botkina
     * <p>
     * As a starting point the geometric center calculated by
     * {@link Centroid#geometricCenter} is used.
     *
     * @param vectors vertices of a polygon
     * @return chebychev center
     */
    public static Vector chebychevCenter(List<Vector> vectors) {
        // Use geometric center as starting point
        Vector x = geometricCenter(vectors);

        List<Vector> e = new ArrayList<>();
        List<Vector> i = new ArrayList<>();

        while (true) {
            // Calculate max distance squared from
            // any point to current center
            double dMax2 = 0;
            for (Vector vector : vectors) {
                double d2 = Vector.dst2(vector, x);
                if (d2 > dMax2) {
                    dMax2 = d2;
                }
            }

            // Calculate all points that have the max distance
            // from the center with a tolerance of 0.05
            e.clear();

            for (Vector vector : vectors) {
                if (Math.abs(Vector.dst2(vector, x) - dMax2) < 0.05) {
                    e.add(vector);
                }
            }

            // If all vector have the same distance, the center
            // has been found
            if (e.size() == vectors.size()) {
                return x;
            }

            // Recursively calculate center
            Vector y = chebychevCenter(e);


            if (x.equals(y)) {
                return x;
            }

            Vector xy = Vector.sub(y, x);

            i.clear();
            for (Vector vector : vectors) {
                if (!e.contains(vector) && xy.dot(Vector.sub(vector, y)) < 0) {
                    i.add(vector);
                }
            }

            double ffk = Double.MAX_VALUE;
            for (Vector vector : i) {
                double t = (Vector.dst2(vector, x) - dMax2) / (2 * xy.dot(Vector.sub(vector, y)));

                if (t < ffk) {
                    ffk = t;
                }
            }

            if (ffk >= 1) {
                return y;
            }

            x = x.add(xy.mult(ffk));
        }
    }
}
