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
        add(facet);
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

    /**
     * Calculates the corners of the given facets. This is done by firstly searching the
     * edges that are only part of one facet and collecting the individual vertices. The
     * chebychev center is the searched and the vertices are sorted by their polar angle
     * with the center as the origin. As a last step fully included facets (all three
     * are corners) are searched and removed. This process gets repeated until the facet
     * count does not change
     */
    public void calculateHull() {
        removeDoubles();

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

        for (int i = corners.size() - 1; i > 1; i--) {
            Vector a = corners.get(i);
            Vector b = corners.get(i - 1);
            Vector c = corners.get(i - 2);

            if (MathUtil.equals((b.y - a.y) * (c.x - b.y), (c.y - b.y) * (b.x - a.x))) {
                corners.remove(i - 1);
            }
        }

        findCenterOfCorners();
    }

    /**
     * Removes facets that are in the set more than once
     */
    private void removeDoubles() {
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
     *
     * @return chebychev center of corners
     */
    private Vector findCenterOfCorners() {
        if (corners.size() == 0) {
            calculateHull();
        }

        List<Vector> corner2D = new ArrayList<>(corners.size());
        for (Vector v : corners) {
            v = v.copy();
            v.z = 0;
            corner2D.add(v);
        }

        return Centroid.chebychevCenter(corner2D);
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
        return facetGroups;
    }

    public List<Facet> getFacets() {
        return facets;
    }

}
