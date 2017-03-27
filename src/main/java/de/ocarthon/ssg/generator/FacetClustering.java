package de.ocarthon.ssg.generator;

import de.ocarthon.ssg.math.Centroid;
import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.FacetGroup;
import de.ocarthon.ssg.math.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class FacetClustering {
    // Default normal vector to prevent normal vector calculation
    private static final Vector DEFAULT_NORMAL = new Vector(0, 0, 0);

    // max distance to center radius squared
    private static final double MAX_R2 = 1;

    private static final int MAX_CLUSTER = 20;

    private static final int MAX_TRIES = 10;

    private static final int MAX_DEPTH = 500;

    private static final double CLUSTER_TOL = 1e-4;

    public static List<FacetGroup> cluster(List<FacetGroup> fgs, double maxDistance) {
        List<FacetGroup> clustered = new ArrayList<>(fgs.size());

        for (FacetGroup fg : fgs) {
            clustered.addAll(cluster(fg, maxDistance));
        }

        return clustered;
    }


    public static List<FacetGroup> cluster(FacetGroup fg, double maxDistance) {
        double maxDst2 = Math.pow(maxDistance, 2);

        if (isFacetGroupValid(fg, maxDst2)) {
            ArrayList<FacetGroup> fgList = new ArrayList<>();
            fgList.add(fg);
            return fgList;
        }

        List<Facet> splitFacets = new ArrayList<>(fg.getFacets().size());

        for (Facet f : fg.getFacets()) {
            splitFacet(f, splitFacets);
        }

        clusterLoop:
        for (int i = 2; i <= MAX_CLUSTER; i++) {
            List<FacetGroup> fgList = clusterFacets(splitFacets, i);

            for (FacetGroup fg1 : fgList) {
                if (!isFacetGroupValid(fg1, maxDst2)) {
                    continue clusterLoop;
                }
            }

            return fgList;
        }

        return Collections.emptyList();
    }

    private static boolean isFacetGroupValid(FacetGroup fg, double maxDst2) {
        // Check if all corners are within the max distance from the center
        fg.calculateHull();

        for (Vector corner : fg.corners) {
            if (Vector.dst2XY(corner, fg.center) > maxDst2) {
                return false;
            }
        }


        return true;
    }

    public static List<FacetGroup> clusterFacets(List<Facet> facets, int clusterCount) {
        WeakHashMap<Facet, Vector> centers = new WeakHashMap<>(facets.size());
        for (Facet f : facets) {
            centers.put(f, Centroid.chebychevCenter(f.toList()));
        }

        int tries = 0;
        double minDist2 = Double.MAX_VALUE;
        List<FacetGroup> fgList = null;

        while (tries++ < MAX_TRIES) {
            List<FacetGroup> currentFg = clusterFacetsOnce(facets, clusterCount);

            double maxDist2 = 0;
            for (FacetGroup fg : currentFg) {
                Vector center = fg.center;
                for (Facet f : fg.getFacets()) {
                    maxDist2 = Math.max(maxDist2, Vector.dst2XY(center, centers.get(f)));
                }
            }

            if (maxDist2 < minDist2) {
                fgList = currentFg;
                minDist2 = maxDist2;
            }
        }

        return fgList;
    }

    private static List<FacetGroup> clusterFacetsOnce(List<Facet> facets, int clusterCount) {
        if (facets.size() < clusterCount) {
            clusterCount = facets.size();
        }

        Random random = new Random();
        List<FacetGroup> cluster = new ArrayList<>(clusterCount);
        for (int i = 0; i < clusterCount; i++) {
            FacetGroup fg = new FacetGroup(facets.get(random.nextInt(facets.size())));
            fg.calculateHull();
            cluster.add(fg);
        }

        int depth = 0;
        while (depth++ < MAX_DEPTH) {
            for (FacetGroup fg : cluster) {
                fg.getFacets().clear();
            }

            for (Facet f : facets) {
                List<Vector> vectors = f.toList();

                double d = Double.MAX_VALUE;
                FacetGroup cfg = null;
                for (FacetGroup fg : cluster) {
                    double e = 0;
                    for (Vector v : vectors) {
                        double e1 = Vector.dst2XY(fg.center, v);

                        if (e1 > e) {
                            e = e1;
                        }
                    }

                    if (e < d) {
                        d = e;
                        cfg = fg;
                    }
                }

                if (cfg != null) {
                    cfg.add(f);
                }
            }

            boolean finished = true;
            for (FacetGroup fg : cluster) {
                Vector oldCenter = fg.center;

                if (fg.getFacets().size() != 0) {
                    fg.calculateHull();

                    if (Vector.dst2XY(oldCenter, fg.center) > CLUSTER_TOL) {
                        finished = false;
                    }
                }
            }

            if (finished && depth > 1) {
                break;
            }
        }

        return cluster;
    }

    public static void splitFacet(Facet f, List<Facet> facets) {
        List<Vector> vectors = f.toList();

        Vector center = Centroid.geometricCenter(vectors);

        double dst2 = 0;

        for (Vector v : vectors) {
            dst2 = Math.max(dst2, Vector.dst2XY(v, center));
        }

        if (dst2 <= MAX_R2) {
            facets.add(f);
            return;
        }

        // Split along longest side
        double maxLength = Math.max(Vector.dst2XY(f.p1, f.p2), Math.max(Vector.dst2XY(f.p2, f.p3), Vector.dst2XY(f.p1, f.p3)));
        if (maxLength == Vector.dst2XY(f.p1, f.p2)) {
            splitFacet(new Facet(f.p1, f.p3, Vector.add(f.p1, f.p2).mult(0.5), DEFAULT_NORMAL), facets);
            splitFacet(new Facet(f.p2, f.p3, Vector.add(f.p1, f.p2).mult(0.5), DEFAULT_NORMAL), facets);
        } else if (maxLength == Vector.dst2XY(f.p2, f.p3)) {
            splitFacet(new Facet(f.p2, f.p1, Vector.add(f.p2, f.p3).mult(0.5), DEFAULT_NORMAL), facets);
            splitFacet(new Facet(f.p3, f.p1, Vector.add(f.p2, f.p3).mult(0.5), DEFAULT_NORMAL), facets);
        } else if (maxLength == Vector.dst2XY(f.p1, f.p3)) {
            splitFacet(new Facet(f.p1, f.p2, Vector.add(f.p1, f.p3).mult(0.5), DEFAULT_NORMAL), facets);
            splitFacet(new Facet(f.p3, f.p2, Vector.add(f.p1, f.p3).mult(0.5), DEFAULT_NORMAL), facets);
        }
    }
}
