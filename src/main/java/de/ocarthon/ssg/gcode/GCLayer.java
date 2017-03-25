package de.ocarthon.ssg.gcode;

import static de.ocarthon.ssg.util.FileUtil.write;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.MathUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GCLayer {
    private List<GCInstruction> instructions = new ArrayList<>();
    private Extruder extruder;
    private double layerHeight;
    private double offset;

    @Deprecated
    public GCLayer(double offset, double layerHeight) {
        this.offset = MathUtil.round(offset, 3);
        this.layerHeight = layerHeight;

    }

    public GCLayer(double offset, double layerHeight, Extruder extruder) {
        this.offset = MathUtil.round(offset, 3);
        this.layerHeight = layerHeight;
        this.extruder = extruder;
    }

    public double getOffset() {
        return offset;
    }

    public void add(GCInstruction instruction) {
        instructions.add(instruction);
    }

    public boolean hasContent() {
        return instructions.size() != 0;
    }

    public double writeGCode(OutputStream out, double eOffset, Printer printer) throws IOException {
        write(out, "; GC_LAYER%n");

        double e = calculateValues(printer, eOffset + printer.retractionAmount) - printer.retractionAmount;

        boolean firstG1 = false;

        for (GCInstruction instruction : getInstructions()) {
            if (!firstG1 && instruction instanceof GCInstructions.G1) {
                firstG1 = true;

                write(out, "G1 F%f E%.4f%n", printer.retractionSpeed, eOffset + printer.retractionAmount);
            }

            write(out, "%s%n", instruction.convertToGCode(printer, extruder));
        }

        write(out, "G1 F%f E%.4f%n", printer.retractionSpeed, e);

        // Reset to travel speed
        write(out, "G0 F%f%n", printer.travelSpeed * 60);

        return e;
    }

    public double calculateValues(Printer printer, double eOffset) {
        GCInstructions.G0 last = null;
        GCInstructions.G0 current;
        double e = eOffset;

        for (GCInstruction instruction : instructions) {
            if (!(instruction instanceof GCInstructions.G0)) {
                continue;
            }

            current = ((GCInstructions.G0) instruction);

            // Apply extruder offset
            //current.x += extruder.nozzleOffsetX;
            //current.y += extruder.nozzleOffsetY;

            if (last == null) {
                current.z = offset;
            }

            if (current instanceof GCInstructions.G1) {
                if (last != null) {
                    if (current instanceof GCInstructions.G2) {
                        GCInstructions.G2 g2 = ((GCInstructions.G2) current);
                        double radius = Math.sqrt(Math.pow(g2.i, 2) + Math.pow(g2.j, 2));
                        e += (8 * layerHeight * extruder.nozzleSize * radius * extruder.materialFlow) / (100 * Math.pow(extruder.materialDiameter, 2));
                    } else {
                        double distance = Math.sqrt(Math.pow(last.x - current.x, 2) + Math.pow(last.y - current.y, 2));
                        e += (distance * layerHeight * extruder.nozzleSize * 4 * extruder.materialFlow) / (100 * Math.PI * Math.pow(extruder.materialDiameter, 2));
                    }

                    ((GCInstructions.G1) current).e = e;
                }

                if (current.f == -1) {
                    current.f = printer.printSpeed * 30;
                }
            }

            if (current.f == -1) {
                current.f = printer.travelSpeed * 30;
            }

            last = current;
        }

        return e;
    }

    public double getLength() {
        GCInstructions.G0 last = null;
        double dst = 0;

        for (GCInstruction instruction : instructions) {
            if (instruction instanceof GCInstructions.G0) {
                GCInstructions.G0 g = ((GCInstructions.G0) instruction);
                if (g instanceof GCInstructions.G1 && last != null) {
                    dst += Math.sqrt(Math.pow(last.x - g.x, 2) + Math.pow(last.y - g.y, 2));
                }

                last = g;
            }
        }

        return dst;
    }

    public List<GCInstruction> getInstructions() {
        return instructions;
    }

    public Extruder getExtruder() {
        return extruder;
    }
}
