package de.ocarthon.ssg.math;

import java.util.ArrayList;
import java.util.List;

public class Object3D {
    public List<Facet> facets;
    public Vector translation = new Vector(0, 0, 0);
    public Vector rotation = new Vector(0, 0, 0);
    public double scalar = 1;

    public Object3D(int facetCount) {
        facets = new ArrayList<>(facetCount);
    }

    public void centerObject() {
        Vector avg = new Vector(0, 0, 0);
        double zMin = Double.MAX_VALUE;
        double temp;

        for (Facet f : facets) {
            avg.add(f.p1).add(f.p2).add(f.p3);
            temp = MathUtil.findLowestPoint(f);

            zMin = Math.min(zMin, temp);
        }

        avg.scale(1d/(facets.size() * 3));

        double finalZMin = zMin;
        facets.forEach(f -> {
            f.p1.sub(avg.x, avg.y, finalZMin);
            f.p2.sub(avg.x, avg.y, finalZMin);
            f.p3.sub(avg.x, avg.y, finalZMin);
        });
    }

    @Override
    public String toString() {
        return "Object3D{" +
                "facets=" + facets +
                ", translation=" + translation +
                ", rotation=" + rotation +
                ", scalar=" + scalar +
                '}';
    }
}
