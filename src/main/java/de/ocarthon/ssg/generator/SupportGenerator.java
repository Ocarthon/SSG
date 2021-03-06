package de.ocarthon.ssg.generator;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.gcode.GCInstruction;
import de.ocarthon.ssg.gcode.GCInstructions;
import de.ocarthon.ssg.gcode.GCLayer;
import de.ocarthon.ssg.gcode.GCObject;
import de.ocarthon.ssg.gcode.GCStructures;
import de.ocarthon.ssg.gcode.splicer.Splicer;
import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.FacetGroup;
import de.ocarthon.ssg.math.MathUtil;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.math.Timer;
import de.ocarthon.ssg.math.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("Duplicates")
public class SupportGenerator {
    private static List<FacetGroup> facetGroups = new ArrayList<>(2);
    private static Object3D object;

    // Maximaler Winkel zwischen Ecken
    private static final double maxCornerAngle = Math.toRadians(20);

    // Minimaler Abstand von Stützstruktur zu Objekt
    private static final double minObjDistanceZ = 1;

    private static final double minObjDistanceXY = 0.8;

    // Radius des Stammes
    private static final double minPillarRadius = 3;
    private static final double pillarOptimisationStep = 0.1;

    // Breite des Stammes in Bahnen
    private static final int pillarWidth = 1;

    // Anzahl der Ecken eines Kreises
    private static final int circleCorners = 20;

    private static final int basisCorners = circleCorners * 3;

    // Höhe der Basis
    private static final double basisHeight = 2;

    // Breite der Basis
    private static final double basisRadius = 4;

    // Distanz zwischen Füllungslinien
    private static final double infillDistance = 8;

    private static final int connectingLayers = 10;

    private static final double minArea = 1;

    private static final double maxDst = 23;


    private static double supportMaxHeight = 0;


    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);

        // Arg checking
        if (args == null || args.length == 0) {
            System.out.println("No object file specified");
            return;
        }


        // Printer configuration
        Printer printer = Printer.k8400();
        printer.useDualPrint = false;
        printer.infillDensity = 3;


        // Create filename
        String fileName;
        if (args.length != 2) {
            fileName = args[0].split("\\.")[0] + "_struc" + (printer.useDualPrint ? "_dual" : "") + ".gcode";
            System.out.println("Using filename: " + fileName);
        } else {
            fileName = args[1];
        }

        File fileOut = new File(fileName);


        // Timer used for stats
        Timer timer = new Timer();
        timer.start();


        // Read object and apply transformations
        System.out.print("Reading object ");
        File file = new File(args[0]);
        object = ObjectReader.readObject(file);
        object.centerObject();

        //object.scale = 1.5;
        object.applyScaleAndRotation();
        System.out.println("[" + timer.next() + "ms]");


        GCObject structObj = new GCObject();


        // Overhang detection
        System.out.print("Searching overhangs ");
        supportMaxHeight = Double.MAX_VALUE;
        object.facets.stream().filter(f -> Vector.angle(f.n, Vector.Z) >= Math.toRadians(90 + printer.supportAngle) && !MathUtil.equals(f.findLowestZ(), 0)).forEach(f -> {
            supportMaxHeight = Math.min(supportMaxHeight, f.findLowestZ());

            boolean a = false;
            for (FacetGroup fg : facetGroups) {
                if (fg.isPart(f)) {
                    fg.add(f.copy());
                    a = true;
                }
            }

            if (!a) {
                facetGroups.add(new FacetGroup(f.copy()));
            }
        });

        supportMaxHeight = roundDownToLayer(supportMaxHeight - minObjDistanceZ, printer);
        System.out.println("[" + timer.next() + "ms]");


        // unify overhang regions
        System.out.print("Unifying overhang regions... ");
        facetGroups = FacetGroup.unifyFacetGroups(facetGroups);
        System.out.println(facetGroups.size() + " region(s) found [" + timer.next() + "ms]");


        // Cluster regions
        System.out.print("Clustering regions... ");
        for (FacetGroup fg : facetGroups) {
            for (Facet f : fg.getFacets()) {
                f.p1.z = supportMaxHeight;
                f.p2.z = supportMaxHeight;
                f.p3.z = supportMaxHeight;
            }
        }
        facetGroups = FacetClustering.cluster(facetGroups, maxDst);
        System.out.println(facetGroups.size() + " region(s) [" + timer.next() + "ms]");


        // generate support structure for every region
        System.out.print("Generating support structure ");
        for (FacetGroup fg : facetGroups) {
            if (fg.getArea() >= minArea) {
                structObj.merge(generateStructure(fg.copy(), printer));
            }
        }
        System.out.println("[" + timer.next() + "ms]");


        // Slice and splice
        System.out.print("Slicing and splicing object ");
        Splicer.sliceAndSplice(object, structObj, supportMaxHeight, printer, fileOut);
        System.out.println("[" + timer.next() + "ms]");

        /*FileOutputStream fos = new FileOutputStream("struct.gcode");
        structObj.exportInstructions(fos, printer);
        fos.flush();
        fos.close();*/
    }

    private static GCObject generateStructure(FacetGroup fg, Printer printer) {
        double radius = minPillarRadius - pillarOptimisationStep;

        GCObject best = null;
        double bestScore = Double.MAX_VALUE;

        while (true) {
            radius += pillarOptimisationStep;

            GCObject obj = generateStructure(fg, printer, radius);

            if (best == null) {
                best = obj;
                bestScore = calculateScoreForGCObject(obj);
            } else {
                double score = calculateScoreForGCObject(obj);

                if (score < bestScore) {
                    best = obj;
                    bestScore = score;
                } else {
                    return best;
                }
            }
        }
    }

    private static double calculateScoreForGCObject(GCObject obj) {
        double score = 0;

        for (GCLayer layer : obj.getLayers()) {
            Vector position = new Vector(0, 0, 0);

            for (GCInstruction instruction : layer.getInstructions()) {
                if (!(instruction instanceof GCInstructions.G0)) {
                    continue;
                }

                GCInstructions.G0 g0 = ((GCInstructions.G0) instruction);

                if (instruction instanceof GCInstructions.G2) {
                    GCInstructions.G2 g2 = ((GCInstructions.G2) instruction);
                    score += 4 * Math.pow(Math.PI, 2) * (Math.pow(position.x - g2.i, 2) + Math.pow(position.y - g2.j, 2));
                } else if (instruction instanceof GCInstructions.G1) {
                    score += Math.pow(position.x - g0.x, 2) + Math.pow(position.y - g0.y, 2);
                    position.set(g0.x, g0.y, 0);
                }
            }
        }

        return score;
    }

    private static GCObject generateStructure(FacetGroup fg, Printer printer, double pillarRadius) {
        GCObject obj = new GCObject();

        Extruder extruder = printer.getExtruder(0);

        // Searches corners
        fg.removeDoubles();
        fg.calculateHull();

        // Save center
        Vector m = fg.center;

        // Add sub corners
        Vector.sortByPolarAngle(fg.corners, m);
        ArrayList<Vector> newCorners = new ArrayList<>();

        for (int i = 0; i < fg.corners.size(); i++) {
            newCorners.add(fg.corners.get(i));
            Vector a = fg.corners.get(i);
            Vector b = fg.corners.get((i + 1) % fg.corners.size());

            double angle = Vector.angle(Vector.sub(a, m), Vector.sub(b, m));
            if (angle > maxCornerAngle) {
                int parts = (int) Math.ceil(angle / maxCornerAngle);
                Vector ab = Vector.sub(b, a).mult(1d / parts);
                for (int j = 1; j <= parts - 1; j++) {
                    newCorners.add(Vector.add(a, ab.copy().mult(j)));
                }
            }
        }
        fg.corners = newCorners;
        Vector.sortByPolarAngle(fg.corners, m);

        // Max height
        double h = supportMaxHeight;

        // hopper height hT
        double hT = Double.MIN_VALUE;
        for (Vector c : fg.corners) {
            // Move corner closer to center point
            Vector mc = Vector.sub(c, m);
            mc.z = 0;
            mc.setMag(mc.lengthXY() - minObjDistanceXY);
            mc.z = h;
            c.set(Vector.add(m, mc));

            double hT1 = c.copy().sub(m).lengthXY() - pillarRadius - minObjDistanceXY;
            if (hT1 > hT) {
                hT = hT1;
            }
        }

        hT = roundDownToLayer(hT / Math.tan(Math.toRadians(printer.supportAngle)), printer);

        double pillarHeight = generateBasis(fg, obj, pillarRadius, printer, printer.useDualPrint ? printer.getExtruder(1) : extruder);
        while (pillarHeight <= (h - hT)) {
            GCLayer layer = obj.newLayer(pillarHeight, printer.layerHeight, extruder);

            // Stamm
            for (int i = 0; i < pillarWidth; i++) {
                GCStructures.circle(printer, layer, m.x, m.y, pillarRadius - i * extruder.nozzleSize, circleCorners);
            }

            pillarHeight += printer.layerHeight;
        }


        // 3.3.4 Trichter inklusive Füllung generieren
        List<Vector> cornerBases = new ArrayList<>(fg.corners.size());
        List<Vector> cornerDir = new ArrayList<>(fg.corners.size());

        for (int i = 0; i < fg.corners.size(); i++) {
            Vector corner = fg.corners.get(i);
            Vector mc = new Vector(corner.x - m.x, corner.y - m.y, 0).norm();
            Vector cornerBase = m.copy().add(mc.copy().mult(pillarRadius));
            cornerBase.z = h - hT;
            cornerBases.add(cornerBase);

            Vector cornerD = Vector.sub(corner, cornerBase);
            cornerDir.add(cornerD.mult(1d / cornerD.z));
        }

        int hopperLayer = 1;
        while (hopperLayer * printer.layerHeight < hT) {
            double hopperHeight = (hopperLayer) * printer.layerHeight;
            GCLayer layer = obj.newLayer(h - hT + hopperHeight, printer.layerHeight, extruder);
            List<Vector> currentCorners = new ArrayList<>();

            for (int i = 0; i <= fg.corners.size(); i++) {
                Vector cb = cornerBases.get(i % fg.corners.size());
                Vector cd = cornerDir.get(i % fg.corners.size());
                Vector p = Vector.add(cb, cd.copy().mult(hopperHeight));

                if (hopperLayer % 2 == 0) {
                    currentCorners.add(p);
                }

                if (i == 0) {
                    layer.add(new GCInstructions.G0(p.x, p.y, p.z));
                } else {
                    layer.add(new GCInstructions.G1(p.x, p.y, p.z));
                }
            }


            if (hopperLayer % 2 == 0) {
                double ymin = Double.MAX_VALUE;
                double ymax = -Double.MAX_VALUE;
                double xmax = -Double.MAX_VALUE;
                double xmin = Double.MAX_VALUE;

                for (Vector v : currentCorners) {
                    if (v.y > ymax) {
                        ymax = v.y;
                    }

                    if (v.y < ymin) {
                        ymin = v.y;
                    }

                    if (v.x > xmax) {
                        xmax = v.x;
                    }

                    if (v.x < xmin) {
                        xmin = v.x;
                    }
                }

                double y0 = m.y - Math.floor(Math.abs(m.y - ymin) / infillDistance) * infillDistance;
                int lines = (int) Math.floor((ymax - ymin) / infillDistance + 1);

                for (int i = 0; i < lines; i++) {
                    double dxpos = xmax - m.x;
                    double dxneg = xmin - m.x;

                    // ymin und ymax bestimmen
                    double y = y0 + i * infillDistance;

                    if (y > ymax) {
                        continue;
                    }

                    for (int j = 0; j < currentCorners.size() - 1; j++) {
                        Vector g2 = currentCorners.get(j);
                        Vector r2 = currentCorners.get((j + 1) % currentCorners.size()).copy().sub(g2);

                        Vector p = MathUtil.solveLSE(1, -r2.x, g2.x - 0, 0, -r2.y, g2.y - y);

                        // No intersection
                        if (p != null) {
                            double dx = p.x - m.x;

                            if (dx > 0 && dx < dxpos) {
                                dxpos = dx;
                            } else if (dx < 0 && dx > dxneg) {
                                dxneg = dx;
                            }
                        }
                    }

                    if (i % 2 == 0) {
                        GCStructures.line(layer, m.x + dxpos - extruder.nozzleSize / 2, y, m.x + dxneg + extruder.nozzleSize / 2, y);
                    } else {
                        GCStructures.line(layer, m.x + dxneg + extruder.nozzleSize / 2, y, m.x + dxpos - extruder.nozzleSize / 2, y);
                    }
                }
            }
            hopperLayer++;
        }

        return obj;
    }

    private static double generateBasis(FacetGroup fg, GCObject structObj, double pillarRadius, Printer printer, Extruder extruder) {
        double r2 = pillarRadius * pillarRadius;

        double lowZ = Double.MAX_VALUE;
        for (Facet f : fg.getFacets()) {
            double lowF = f.findLowestZ();
            if (lowF < lowZ) {
                lowZ = lowF;
            }
        }

        List<Facet> supportFacets = new ArrayList<>();
        Vector m = fg.center;

        double supLow = Double.MAX_VALUE;
        for (Facet f : object.facets) {
            double z = f.findLowestZ();
            if (z < lowZ && Vector.angle(Vector.Z, f.n) <= Math.PI / 2 && MathUtil.dst2PointTriangle(m, f.p1, f.p2, f.p3) <= r2) {
                supportFacets.add(f);

                if (z < supLow) {
                    supLow = z;
                }
            }
        }

        if (supportFacets.size() == 0) {
            double tanB = Math.tan(basisHeight / basisRadius);
            double height = printer.layerHeight0;

            while (height <= basisHeight) {
                GCLayer layer = structObj.newLayer(height, height == printer.layerHeight0 ? printer.layerHeight0 : printer.layerHeight, extruder);

                int basisCount = (int) Math.floor((basisHeight - height) / (tanB * extruder.nozzleSize));

                if (basisCount > 0) {
                    for (int i = basisCount - 1; i >= -pillarWidth + 1; i--) {
                        GCStructures.circle(printer, layer, m.x, m.y, pillarRadius + i * extruder.nozzleSize, circleCorners);
                    }

                    height += printer.layerHeight;
                } else {
                    break;
                }
            }

            return height;
        } else {
            double alpha = 2 * Math.PI / basisCorners;
            double height = roundDownToLayer(supLow, printer);

            int layers = 0;
            double segments = 0;
            while (segments < basisCorners || ++layers < connectingLayers) {
                segments = 0;

                GCLayer layer = structObj.newLayer(height + printer.layerHeight, printer.layerHeight, extruder);
                for (int i = 0; i < basisCorners; i++) {
                    Vector a = new Vector(m.x + Math.sin(alpha * i) * pillarRadius, m.y + Math.cos(alpha * i) * pillarRadius, height);
                    Vector r = new Vector(Math.sin(alpha * (i + 1)) - Math.sin(alpha * i), Math.cos(alpha * (i + 1)) - Math.cos(alpha * i), 0).mult(pillarRadius);

                    boolean print = true;
                    for (Facet f : supportFacets) {
                        if (MathUtil.doesIntersect(f.p1, f.n, a, r) || !MathUtil.isAbovePlane(a, f.p1, f.n)) {
                            print = false;
                        }
                    }

                    if (print) {
                        segments++;
                        Vector b = Vector.add(a, r);
                        GCStructures.line(layer, a.x, a.y, b.x, b.y);
                    }
                }

                height += printer.layerHeight;
            }

            return height + printer.layerHeight;
        }
    }


    public static double roundDownToLayer(double height, Printer printer) {
        int layerAbove0 = (int) Math.floor((height - printer.layerHeight0) / printer.layerHeight);

        return printer.layerHeight0 + layerAbove0 * printer.layerHeight;
    }


}
