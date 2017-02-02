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

    public void removeDoubles() {
        HashSet<Facet> set = new HashSet<>();
        for (Facet f : facets) {
            if (!set.contains(f)) {
                set.add(f);
            }
        }

        facets.clear();

        for (Facet aSet : set) {
            facets.add(aSet);
        }
    }

    public void calculateCorners() {
        HashMap<VectorPair, Integer> counts = new HashMap<>();
        for (Facet f : facets) {
            VectorPair vp = new VectorPair(f.p1, f.p2);
            if (counts.containsKey(vp)) {
                int k = counts.get(vp);
                counts.put(vp, ++k);
            } else {
                counts.put(vp, 1);
            }

            vp = new VectorPair(f.p1, f.p3);
            if (counts.containsKey(vp)) {
                int k = counts.get(vp);
                counts.put(vp, ++k);
            } else {
                counts.put(vp, 1);
            }

            vp = new VectorPair(f.p2, f.p3);
            if (counts.containsKey(vp)) {
                int k = counts.get(vp);
                counts.put(vp, ++k);
            } else {
                counts.put(vp, 1);
            }
        }

        for (VectorPair vp : counts.keySet()) {
            if (counts.get(vp) == 1) {
                if (!corners.contains(vp.v1)) {
                    corners.add(vp.v1);
                }

                if (!corners.contains(vp.v2)) {
                    corners.add(vp.v2);
                }

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

    public Vector findCenterOfCorners() {
        if (corners.size() == 0) {
            calculateCorners();
        }

        /*double x = 0;
        double y = 0;
        double z = 0;

        for (Vector v : corners) {
            x += v.x;
            y += v.y;
            z += v.z;
        }

        return new Vector(x / corners.size(), y / corners.size(), z / corners.size());*/
        return Centroid.calculateCentroid(corners);
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
