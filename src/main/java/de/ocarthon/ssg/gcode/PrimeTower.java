package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.Vector;

import java.io.IOException;
import java.io.OutputStream;

public class PrimeTower {
    private final Printer printer;

    private int currentLayer = 1;

    public PrimeTower(Printer printer) {
        this.printer = printer;
    }

    public double getCurrentHeight() {
        return printer.layerHeight * (currentLayer + 1);
    }

    public double printLayer(OutputStream out, Printer printer, Extruder ext, double size) throws IOException {
        double e = ext.isPrimed ? printer.retractionAmount : 0;

        int lanes = (int) Math.floor(size / ext.nozzleSize);
        double filamentPerLane = (size * printer.layerHeight * ext.nozzleSize * 4 * ext.materialFlow) / (100 * Math.PI * Math.pow(ext.materialDiameter, 2));

        Vector p1 = new Vector(0, 0, 0);
        Vector p2 = new Vector(0, 0, 0);

        for (int i = 0; i < lanes; i++) {
            e += filamentPerLane;

            if (currentLayer % 2 == 0) {
                p1.x = printer.primeTowerX + ext.nozzleOffsetX;
                p1.y = printer.primeTowerY + (i + 1) * ext.nozzleSize + ext.nozzleOffsetY;

                p2.x = printer.primeTowerX + size + ext.nozzleOffsetX;
                p2.y = p1.y;
            } else {
                p1.x = printer.primeTowerX + (i + 1) * ext.nozzleSize + ext.nozzleOffsetX;
                p1.y = printer.primeTowerY + ext.nozzleOffsetY;

                p2.x = printer.primeTowerX + (i + 1) * ext.nozzleSize + ext.nozzleOffsetX;
                p2.y = printer.primeTowerY + size + ext.nozzleOffsetY;
            }

            if (i % 2 == 0) {
                if (i == 0) {
                    out.write(String.format("G0 F%f X%.5f Y%.5f%n", printer.travelSpeed * 30, p1.x, p1.y).getBytes());
                    out.write(String.format("G0 F%f Z%.5f%n", printer.travelSpeed * 30, currentLayer * printer.layerHeight).getBytes());

                    if (ext.isPrimed) {
                        out.write(String.format("G1 F1500 E%.5f%n", printer.retractionAmount).getBytes("UTF-8"));
                    }
                } else {
                    out.write(String.format("G0 F%f X%.5f Y%.5f%n", printer.travelSpeed * 60, p1.x, p1.y).getBytes());
                }

                out.write(String.format("G1 F%f X%.5f Y%.5f E%.5f%n", printer.printSpeed * 30, p2.x, p2.y, e).getBytes());
            } else {
                out.write(String.format("G0 F%f X%.5f Y%.5f%n", printer.travelSpeed * 60, p2.x, p2.y).getBytes());
                out.write(String.format("G1 F%f X%.5f Y%.5f E%.5f%n", printer.printSpeed * 30, p1.x, p1.y, e).getBytes());
            }

        }

        e -= printer.retractionAmount;
        out.write(String.format("G1 F1500 E%.5f%n", e).getBytes("UTF-8"));
        ext.isPrimed = true;

        currentLayer++;
        return e;
    }
}
