package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Printer;

public final class GCStructures {

    public static void line(GCLayer layer, double x1, double y1, double x2, double y2) {
        layer.add(new GCInstructions.G0(x1, y1));
        layer.add(new GCInstructions.G1(x2, y2));
    }

    public static void circle(Printer printer, GCLayer layer, double x, double y, double radius, int n) {
        if (printer.useG2) {
            layer.add(new GCInstructions.G0(x, y + radius));
            layer.add(new GCInstructions.G2(0, -radius));
        } else {
            double arc = (2 * Math.PI) / (double) n;
            for (int i = 0; i <= n; i++) {
                if (i == 0) {
                    layer.add(new GCInstructions.G0(x, y + radius));
                } else {
                    layer.add(new GCInstructions.G1(x + Math.sin(arc * (double) i) * radius, y + Math.cos(arc * (double) i) * radius));
                }
            }
        }
    }
}
