package de.ocarthon.ssg.math;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FacetGroup {
    private List<Facet> facets = new ArrayList<>();
    public List<Vector> corners = new ArrayList<>();
    public Vector center = new Vector(0, 0, 0);
    public Color color;

    public FacetGroup(Facet facet, Color color) {
        this.color = color;

        if (facet != null) {
            add(facet);
        }
    }

    public FacetGroup(Facet facet) {
        this(facet, null);
    }

    public FacetGroup copy() {
        FacetGroup fg = new FacetGroup(null, this.color);

        for (Facet f : facets) {
            fg.facets.add(f.copy());
        }

        for (Vector corner : corners) {
            fg.corners.add(corner.copy());
        }

        fg.center = this.center.copy();

        return fg;
    }

    public boolean isPart(Facet f) {
        for (Facet f2 : facets) {
            if (f.p1.equals(f2.p1) || f.p1.equals(f2.p2, 0.1) || f.p1.equals(f2.p3, 0.1)
                    || f.p1.equals(f2.p2, 0.1) || f.p2.equals(f2.p2, 0.1) || f.p2.equals(f2.p3, 0.1)
                    || f.p3.equals(f2.p1, 0.1) || f.p3.equals(f2.p2, 0.1) || f.p3.equals(f2.p3, 0.1)) {
                return true;
            }
        }

        return false;
    }

    public void add(Facet facet) {
        facets.add(facet);
        facet.color = this.color;
    }


    /**
     * Calculates the convex hull via the jarvis algorithm and then
     * calculated the center of the hull
     *
     * Adapted from http://stackoverflow.com/a/10022243/3930389
     */
    public void calculateHull() {
        corners.clear();
        List<Vector> vertices = new ArrayList<>(facets.size());

        // Collect all vertices
        for (Facet f : facets) {
            if (!vertices.contains(f.p1)) {
                vertices.add(f.p1);
            }

            if (!vertices.contains(f.p2)) {
                vertices.add(f.p2);
            }

            if (!vertices.contains(f.p3)) {
                vertices.add(f.p3);
            }
        }

        Vector pOnHull = vertices.get(0);
        for (int i = 1; i < vertices.size(); i++) {
            if (vertices.get(i).y < pOnHull.y) {
                pOnHull = vertices.get(i);
            }
        }

        Vector endpoint = null;
        while (endpoint == null || !endpoint.equals(corners.get(0))) {
            corners.add(pOnHull);
            endpoint = vertices.get(0);

            for (int i = 1; i < vertices.size(); i++) {
                Vector p = vertices.get(i);
                if (pOnHull.equals(endpoint) || (endpoint.x - pOnHull.x) * (p.y - pOnHull.y) - (p.x - pOnHull.x) * (endpoint.y - pOnHull.y) > 0) {
                    endpoint = p;
                }
            }

            pOnHull = endpoint;
        }

        MathUtil.HIGH_TOLERANCE = true;

        for (int i = corners.size() - 1; i > 1; i--) {
            Vector a = corners.get(i);
            Vector b = corners.get(i - 1);
            Vector c = corners.get(i - 2);

            /*if (MathUtil.equals((b.y - a.y) * (c.x - b.y), (c.y - b.y) * (b.x - a.x))) {
                corners.remove(i - 1);
            }*/

            Vector ab = Vector.sub(b, a).norm();
            Vector bc = Vector.sub(c, b).norm();

            if (Vector.dst2(ab, bc) < 0.01) {
                corners.remove(i - 1);
            }
        }

        MathUtil.HIGH_TOLERANCE = false;

        findCenterOfCorners();
    }

    /**
     * Removes facets that are in the set more than once
     */
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

    /**
     * Calculates the chebychev center of the current corners in 2D
     */
    private void findCenterOfCorners() {
        if (corners.size() == 0) {
            calculateHull();
        }

        List<Vector> corner2D = new ArrayList<>(corners.size());
        for (Vector v : corners) {
            v = v.copy();
            v.z = 0;
            corner2D.add(v);
        }

        center = Centroid.chebychevCenter(corner2D);
    }

    // http://math.stackexchange.com/q/516223
    public double getArea() {
        double area = 0;
        for (Facet f : facets) {
            area += Math.abs((f.p1.x - f.p3.x) * (f.p2.y - f.p1.y) - (f.p1.x - f.p2.x) * (f.p3.y - f.p1.y));
        }

        return area / 2;
    }


    public static List<FacetGroup> unifyFacetGroups(List<FacetGroup> facetGroups) {
        MathUtil.HIGH_TOLERANCE = true;

        int oldLength = facetGroups.size();
        int newLength = oldLength + 1;

        while (oldLength != newLength) {
            ArrayList<FacetGroup> newGroup = new ArrayList<>();
            newGroup.add(facetGroups.get(0));
            l:
            for (int i = 1; i < facetGroups.size(); i++) {
                for (Facet f : facetGroups.get(i).getFacets()) {
                    for (FacetGroup fg : newGroup) {
                        if (fg.isPart(f)) {
                            for (Facet f1 : facetGroups.get(i).getFacets()) {
                                fg.add(f1);
                            }
                            continue l;
                        }
                    }
                }

                newGroup.add(facetGroups.get(i));
            }

            oldLength = facetGroups.size();
            newLength = newGroup.size();

            facetGroups = newGroup;
        }

        MathUtil.HIGH_TOLERANCE = false;

        for (FacetGroup fg : facetGroups) {
            fg.removeDoubles();
        }

        return facetGroups;
    }

    public List<Facet> getFacets() {
        return facets;
    }

}
