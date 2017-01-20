package de.ocarthon.ssg.math;

import java.awt.Color;
import java.util.*;

public class FacetGroup {
    private List<Facet> facets = new ArrayList<>();
    public List<Vector> corners = new ArrayList<>();
    public Color color;

    public FacetGroup(Facet facet, Color color) {
        this.color = color;
        add(facet);
    }

    public FacetGroup(Facet facet) {
        add(facet);
    }

    public boolean isPart(Facet f) {
        for (Facet f2 : facets) {
            if (f.p1.equals(f2.p1) || f.p1.equals(f2.p2) || f.p1.equals(f2.p3)
                    || f.p1.equals(f2.p2) || f.p2.equals(f2.p2) || f.p2.equals(f2.p3)
                    || f.p3.equals(f2.p1) || f.p3.equals(f2.p2) || f.p3.equals(f2.p3)) {
                return true;
            }
        }

        return false;
    }

    public void add(Facet facet) {
        facets.add(facet);
        facet.color = this.color;
    }

    public void calculateCorners() {
        HashMap<Vector, Integer> counts = new HashMap<>();
        for (int i = 0; i < facets.size(); i++) {
            Facet f1 = facets.get(i);

            if (counts.containsKey(f1.p1)) {
                int k = counts.get(f1.p1);
                counts.put(f1.p1, ++k);
            } else {
                counts.put(f1.p1, 1);
            }

            if (counts.containsKey(f1.p2)) {
                int k = counts.get(f1.p2);
                counts.put(f1.p2, ++k);
            } else {
                counts.put(f1.p2, 1);
            }

            if (counts.containsKey(f1.p3)) {
                int k = counts.get(f1.p3);
                counts.put(f1.p3, ++k);
            } else {
                counts.put(f1.p3, 1);
            }
        }

        for (Vector v : counts.keySet()) {
            if (counts.get(v) <= 2) {
                corners.add(v);
            }
        }
    }

    public double lowestPosition() {
        double min = Double.MAX_VALUE;

        for (Facet f : facets) {
            if (f.p1.z < min) {
                min = f.p1.z;
            }

            if (f.p2.z < min) {
                min = f.p2.z;
            }

            if (f.p3.z < min) {
                min = f.p3.z;
            }
        }

        return min;
    }

    public Vector findSupportBaseVector(List<Facet> facets) {
        Vector center = findCenter();
        double height = 0;
        for (Facet f : facets) {
            if (this.facets.contains(f)) {
                continue;
            }

            Vector b = MathUtil.barycentricCoordinates(f, center);
            if (b.x >= 0 && b.x <= 1 && b.y >= 0 && b.y <= 1 && b.z >= 0 && b.z <= 1) {
                double tHeight = b.x * f.p1.z + b.y * f.p2.z + b.z * f.p3.z;
                //System.out.println("tHeight " + (b.x+b.y+b.z) +" " +tHeight + f);
                if (tHeight < center.z && tHeight > height) {
                    height = tHeight;
                }
            }
        }

        center.z = height;

        return center;
    }

    public Vector findCenterOfCorners() {
        if (corners.size() == 0) {
            calculateCorners();
        }

        double x = 0;
        double y = 0;
        double z = 0;

        for (Vector v : corners) {
            x += v.x;
            y += v.y;
            z += v.z;
        }

        return new Vector(x / corners.size(), y / corners.size(), z / corners.size());
    }

    public Vector findCenter() {
        double x = 0;
        double y = 0;
        double z = 0;

        for (Facet f : facets) {
            x += (f.p1.x + f.p2.x + f.p3.x) / 3;
            y += (f.p1.y + f.p2.y + f.p3.y) / 3;
            z += (f.p1.z + f.p2.z + f.p3.z) / 3;
        }

        return new Vector(x / facets.size(), y / facets.size(), z / facets.size());
    }

    public List<Facet> getFacets() {
        return facets;
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
