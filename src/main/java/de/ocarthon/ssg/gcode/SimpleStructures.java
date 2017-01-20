package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.gcode.GCInstructions;
import de.ocarthon.ssg.gcode.GCLayer;
import de.ocarthon.ssg.gcode.GCObject;

public final class SimpleStructures {

    public static void generateCircleTower(GCObject object, Extruder extruder,
                                           double zMin, double zMax, double layerHeight,
                                           double x, double y, double radius, int n) {
        for (int i = 0; i < Math.floor((zMax - zMin) / layerHeight); i++) {
            GCLayer layer = object.newLayer(zMin + (i + 1) * layerHeight, layerHeight, extruder);
            generateCircle(layer, x, y, radius, n);
        }
    }

    public static void generateCircle(GCLayer layer, double x, double y, double radius, int n) {
        double arc = (2 * Math.PI) / (double) n;
        for (int i = 0; i <= n; i++) {
            if (i == 0) {
                layer.add(new GCInstructions.Move(x, y + radius));
            } else {
                layer.add(new GCInstructions.Print(x + Math.sin(arc * (double) i) * radius, y + Math.cos(arc * (double) i) * radius));
            }
        }
    }
}
