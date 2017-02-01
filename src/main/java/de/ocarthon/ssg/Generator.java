package de.ocarthon.ssg;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.gcode.*;
import de.ocarthon.ssg.math.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("Duplicates")
public class Generator {
    private static Object3D object;
    private static Vector zAxis = new Vector(0, 0, 1);

    private static ArrayList<FacetGroup> facetGroups = new ArrayList<>(2);

    // Maximaler Winkel zwischen Ecken
    private static final double maxCornerAngle = Math.toRadians(10);

    // Maximaler Überhangswinkel
    private static final double alphaMax = 45;

    // Minimaler Abstand von Stützstruktur zu Objekt
    private static final double minObjDistance = 2;

    // Radius des Stammes
    private static final double pillarRadius = 5;

    // Breite des Stammes in Bahnen
    private static final int pillarWidth = 2;

    // Anzahl der Ecken eines Kreises
    private static final int circleCorners = 64;

    // Höhe der Basis
    private static final double basisHeight = 2;

    // Breite der Basis
    private static final double basisRadius = 4;

    // Distanz zwischen Füllungslinien
    private static final double infillDistance = 4;

    // Minimale Linienlänge
    private static final double infillMinLength = 4;

    private static double supportMaxHeight = 0;


    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);

        if (args == null || args.length == 0) {
            System.out.println("No file specified");
            return;
        }

        if (args.length != 2) {
            System.out.println("Invalid arguments");
            return;
        }

        File file = new File(args[0]);
        object = ObjectReader.readObject(file);
        object.centerObject();

        File fileOut = new File(args[1]);

        Printer printer = new Printer();
        printer.supportAngle = (float) alphaMax;

        Extruder extruder = new Extruder();
        printer.addExtruder(extruder);

        GCObject structObj = new GCObject();


        // 3.3.1 Überhänge erkennen
        // Alle Überhängenden Flächen in Flächengruppen einteilen
        System.out.println("Searching overhangs");
        object.facets.stream().filter(f -> Vector.angle(f.n, zAxis) >= Math.toRadians(90 + alphaMax) && !(MathUtil.findLowestPoint(f) == 0)).forEach(f -> {
            boolean a = false;
            for (FacetGroup fg : facetGroups) {
                if (fg.isPart(f)) {
                    fg.add(f);
                    a = true;
                }
            }

            if (!a) {
                facetGroups.add(new FacetGroup(f));
            }
        });

        // Flächengruppen versuchen zu komprimieren
        System.out.println("Searching overhang regions");
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

        System.out.println("Generate support structures");
        for (FacetGroup fg : facetGroups) {
            generateStructure(fg, structObj, printer, extruder);
        }

        System.out.println("Slicing");
        System.out.println(supportMaxHeight);

        supportMaxHeight = Math.round(supportMaxHeight * 100) / 100D;
        System.out.println(supportMaxHeight);

        /*FileOutputStream fos = new FileOutputStream("struct.gcode");
        structObj.exportInstructions(fos, printer);
        fos.flush();
        fos.close();*/
        Splicer.sliceAndSplice(object, structObj, supportMaxHeight, printer, fileOut);
    }

    public static void generateStructure(FacetGroup fg, GCObject structObj, Printer printer, Extruder extruder) {
        // 3.3.2 Kerngrößen berechnen
        // Eckpunkte finden
        fg.calculateCorners();

        // Mittelpunkt m
        Vector m = fg.findCenterOfCorners();

        // Zwischenecken einfügen
        fg.corners.sort(new FacetGroup.PolarVecComp(m));

        ArrayList<Vector> newCorners = new ArrayList<>();

        for (int i = 0; i < fg.corners.size(); i++) {
            newCorners.add(fg.corners.get(i));
            Vector a = fg.corners.get(i);
            Vector b = fg.corners.get((i+1) % fg.corners.size());

            double angle = Vector.angle(a.copy().sub(m), b.copy().sub(m));
            if (angle > maxCornerAngle) {
                int parts = (int) Math.ceil(angle / maxCornerAngle);
                Vector ab = b.copy().sub(a).scale(1D/(parts));
                for (int j = 1; j <= parts-1; j++) {
                    newCorners.add(a.copy().add(ab.copy().scale(j)));
                }
            }
        }
        fg.corners = newCorners;
        fg.corners.sort(new FacetGroup.PolarVecComp(m));
        System.out.println(fg.corners);

        // Maximale Höhe h
        double h = roundDownToLayer(fg.lowestPosition() - minObjDistance, printer);
        supportMaxHeight = h;

        // Trichterhöhe hT
        double hT = Double.MIN_VALUE;
        for (Vector c : fg.corners) {
            double hT1 = c.copy().sub(m).lengthXY()-pillarRadius;
            if (hT1 > hT) {
                hT = hT1;
            }
        }

        hT = roundDownToLayer(hT / Math.tan(Math.toRadians(alphaMax)), printer);


        // 3.3.3 Stamm und Basis generieren
        double tanB = Math.tan(basisHeight / basisRadius);
        double height = printer.layerHeight0;

        while (height <= (h - hT)) {
            GCLayer layer = structObj.newLayer(height, height == printer.layerHeight0 ? printer.layerHeight0 : printer.layerHeight, extruder);

            // Stamm
            for (int i = 0; i < pillarWidth; i++) {
                GCStructures.generateCircle(printer, layer, m.x, m.y, pillarRadius - i * extruder.nozzleSize, circleCorners);
            }

            // Basis
            if (height <= basisHeight) {
                int basisCount = (int) Math.floor((basisHeight - height) / (tanB * extruder.nozzleSize));
                for (int i = 0; i < basisCount; i++) {
                    GCStructures.generateCircle(printer, layer, m.x, m.y, pillarRadius + i * extruder.nozzleSize, circleCorners);
                }
            }

            height += printer.layerHeight;
        }

        // 3.3.4 Trichter inklusive Füllung generieren
        List<Vector> cornerBases = new ArrayList<>(fg.corners.size());
        List<Vector> cornerDir = new ArrayList<>(fg.corners.size());

        for (int i = 0; i < fg.corners.size(); i++) {
            Vector corner = fg.corners.get(i);
            Vector mc = new Vector(corner.x - m.x, corner.y - m.y, 0).norm();
            Vector cornerBase = m.copy().add(mc.copy().scale(pillarRadius));
            cornerBase.z = h - hT;
            cornerBases.add(cornerBase);

            Vector cornerD = corner.copy().sub(cornerBase);
            cornerD.scale(1/cornerD.z);
            cornerDir.add(cornerD);
        }

        for (int i = 0; i < fg.corners.size(); i++) {
            System.out.println(fg.corners.get(i) + " " + cornerBases.get(i) + " " + cornerDir.get(i));
        }

        int hopperLayer = 1;
        while (hopperLayer * printer.layerHeight < hT) {
            double hopperHeight = hopperLayer * printer.layerHeight;
            GCLayer layer = structObj.newLayer(h - hT + hopperHeight, printer.layerHeight, extruder);
            List<Vector> currentCorners = new ArrayList<>();

            for (int i = 0; i <= fg.corners.size(); i++) {
                Vector cb = cornerBases.get(i % fg.corners.size());
                Vector cd = cornerDir.get(i % fg.corners.size());
                Vector p = cb.copy().add(cd.copy().scale(hopperHeight));

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
                double ymax = Double.MIN_VALUE;
                double xmax = Double.MIN_VALUE;
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

                double y0 = m.y - Math.floor(Math.abs(m.y-ymin) / infillDistance) * infillDistance;
                int lines = (int) Math.floor((ymax - ymin) / infillDistance);

                for (int i = 0; i < lines; i++) {
                    double xma = Double.MIN_VALUE;
                    double xmi = Double.MAX_VALUE;

                    // ymin und ymax bestimmen
                    double y = y0 + i * infillDistance;

                    for (int j = 0; j < currentCorners.size(); j++) {
                        Vector g2 = currentCorners.get(j);
                        Vector r2 = currentCorners.get((j+1) % currentCorners.size()).copy().sub(g2);

                        Vector p = LGS.solveLGS(1, -r2.x, g2.x-0, 0, -r2.y, g2.y-y);

                        // No intersection
                        if (p == null) {
                            continue;
                        }

                        if (p.x > xma && p.x <= xmax ) {
                            xma = p.x;
                        }

                        if (p.x < xmi && p.x >= xmin) {
                            xmi = p.x;
                        }
                    }

                    if (i % 2 == 0) {
                        layer.add(new GCInstructions.G0(xmi, y));
                        layer.add(new GCInstructions.G1(xma, y));
                    } else {
                        layer.add(new GCInstructions.G0(xma, y));
                        layer.add(new GCInstructions.G1(xmi, y));
                    }
                }
            }
            hopperLayer++;
        }
    }

    public static double roundDownToLayer(double height, Printer printer) {
        int layerAbove0 = (int) Math.floor((height-printer.layerHeight0)/printer.layerHeight);

        return printer.layerHeight0 + layerAbove0 * printer.layerHeight;
    }


}
