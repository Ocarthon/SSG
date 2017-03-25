package de.ocarthon.ssg.gcode;

import static de.ocarthon.ssg.util.FileUtil.write;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.Vector;

import java.io.IOException;
import java.io.OutputStream;

public class PrimeTower {
    private final Printer printer;

    private final Vector position;
    private final double size;

    private int currentLayer = 1;

    public PrimeTower(Printer printer, boolean initPrimeTower) {
        this.printer = printer;

        if (initPrimeTower) {
            this.position = printer.initPrimeTowerPosition;
            this.size = printer.initPrimeTowerSize;
        } else {
            this.position = printer.primeTowerPosition;
            this.size = printer.primeTowerSize;
        }
    }

    public double getCurrentHeight() {
        return printer.layerHeight * (currentLayer + 1);
    }

    public double printLayer(OutputStream out, double e, Extruder ext) throws IOException {
        int lanes = (int) Math.floor(size / ext.nozzleSize);
        double materialPerLine = (size * printer.layerHeight * ext.nozzleSize * 4 * ext.materialFlow) / (100 * Math.PI * Math.pow(ext.materialDiameter, 2));

        Vector p1 = new Vector(0, 0, 0);
        Vector p2 = new Vector(0, 0, 0);

        for (int i = 0; i < lanes; i++) {
            if (currentLayer % 2 == 0) {
                p1.x = position.x + ext.nozzleOffsetX;
                p1.y = position.y + (i + 1) * ext.nozzleSize + ext.nozzleOffsetY;

                p2.x = position.x + size + ext.nozzleOffsetX;
                p2.y = p1.y;
            } else {
                p1.x = position.x + (i + 1) * ext.nozzleSize + ext.nozzleOffsetX;
                p1.y = position.y + ext.nozzleOffsetY;

                p2.x = position.x + (i + 1) * ext.nozzleSize + ext.nozzleOffsetX;
                p2.y = position.y + size + ext.nozzleOffsetY;
            }

            if (i % 2 == 0) {
                if (i == 0) {
                    write(out, "G0 F%f X%.5f Y%.5f Z%.5f%n", printer.travelSpeed * 30, p1.x, p1.y, currentLayer * printer.layerHeight);

                    if (ext.isPrimed) {
                        write(out, "G1 F1500 E%.5f%n", e += printer.retractionAmount);
                    }
                } else {
                    write(out, "G0 F%f X%.5f Y%.5f%n", printer.travelSpeed * 60, p1.x, p1.y);
                }

                write(out, "G1 F%f X%.5f Y%.5f E%.5f%n", printer.printSpeed * 30, p2.x, p2.y, e += materialPerLine);
            } else {
                write(out, "G0 F%f X%.5f Y%.5f%n", printer.travelSpeed * 60, p2.x, p2.y);
                write(out, "G1 F%f X%.5f Y%.5f E%.5f%n", printer.printSpeed * 30, p1.x, p1.y, e += materialPerLine);
            }
        }

        e -= printer.retractionAmount;
        write(out, "G1 F%f E%.5f%n", printer.retractionSpeed, e);
        ext.isPrimed = true;

        // Reset to travel speed
        write(out, "G0 F%f%n", printer.travelSpeed * 60);

        currentLayer++;
        return e;
    }
}
