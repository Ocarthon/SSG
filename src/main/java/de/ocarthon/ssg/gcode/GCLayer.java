package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GCLayer {
    private List<GCInstruction> instructions = new ArrayList<>();
    private Extruder extruder;
    private double layerHeight;
    private double offset;

    @Deprecated
    public GCLayer(double offset, double layerHeight) {
        this.offset = offset;
        this.layerHeight = layerHeight;

    }

    public GCLayer(double offset, double layerHeight, Extruder extruder) {
        this.offset = offset;
        this.layerHeight = layerHeight;
        this.extruder = extruder;
    }

    public double getLayerHeight() {
        return layerHeight;
    }

    public double getOffset() {
        return offset;
    }

    public void add(GCInstruction instruction) {
        instructions.add(instruction);
    }

    public double calculateValues(Printer printer) {
        return calculateValues(printer, 0);
    }

    public double calculateValues(Printer printer, double eOffset) {
        GCInstructions.Move last = null;
        GCInstructions.Move current;
        double e = eOffset;

        for (GCInstruction instruction : instructions) {
            if (!(instruction instanceof GCInstructions.Move)) {
                continue;
            }

            current = ((GCInstructions.Move) instruction);

            // Apply extruder offset
            //current.x += extruder.xOffset;
            //current.y += extruder.yOffset;

            if (last == null) {
                current.z = offset;
            }

            if (current instanceof GCInstructions.Print) {
                if (last != null) {
                    double distance = Math.sqrt(Math.pow(last.x - current.x, 2)+Math.pow(last.y - current.y,2));
                    e += (distance * layerHeight * extruder.nozzleSize * 4 * extruder.materialFlow)/(100 * Math.PI * Math.pow(extruder.materialDiameter, 2));
                    ((GCInstructions.Print) current).e = e;
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

    public void writeGCode(OutputStream out, Printer printer) throws IOException {
        double e = calculateValues(printer);

        boolean firstG0 = false;

        for (GCInstruction instruction : getInstructions()) {
            out.write((instruction.convertToGCode(printer, getExtruder())+"\n").getBytes("UTF-8"));

            if (!firstG0 && instruction instanceof GCInstructions.Move) {
                firstG0 = true;

                if (printer.retractionEnabled()) {
                    out.write(("G92 E-"+printer.retractionAmount+"\n").getBytes());
                    out.write("G1 F1500 E0\n".getBytes());
                } else {
                    out.write("G92 E0".getBytes());
                }
            }
        }

        if (printer.retractionEnabled()) {
            out.write(("G1 F1500 E"+ (e-printer.retractionAmount)+"\n").getBytes());
        }
    }

    public List<GCInstruction> getInstructions() {
        return instructions;
    }

    public Extruder getExtruder() {
        return extruder;
    }
}
