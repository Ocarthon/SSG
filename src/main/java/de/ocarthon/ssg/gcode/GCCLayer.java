package de.ocarthon.ssg.gcode;

import static de.ocarthon.ssg.gcode.GCUtil.E_PATTERN;
import static de.ocarthon.ssg.gcode.GCUtil.readDouble;
import static de.ocarthon.ssg.util.FileUtil.write;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GCCLayer extends GCLayer {
    LinkedList<String> gCode;
    double initialE = 0;

    GCCLayer(LinkedList<String> gCode, double offset, double layerHeight, Extruder extruder) {
        super(offset, layerHeight, extruder);
        this.gCode = gCode;
    }

    @Override
    public void add(GCInstruction instruction) {
    }

    @Override
    public boolean hasContent() {
        return gCode.size() != 0;
    }

    @Override
    public double calculateValues(Printer printer, double eOffset) {
        return 0;
    }


    @Override
    public double writeGCode(OutputStream out, double eOffset, Printer printer) throws IOException {
        boolean startsWRetracion = hasRetraction(true, printer);

        // Set E-Axis
        if (eOffset + (startsWRetracion ? 0 : printer.retractionAmount) != initialE) {
            write(out, "G92 E%.5f%n", initialE - (startsWRetracion ? 0 : printer.retractionAmount));
        }

        boolean firstG1 = false;
        for (String s : gCode) {
            if (!startsWRetracion && !firstG1 && (s.startsWith("G1") || s.startsWith("G2"))) {
                firstG1 = true;
                write(out, "G1 F%f E%.5f%n", printer.retractionSpeed, initialE);
            }

            write(out, "%s%n", s);
        }


        double endE = -1;
        Iterator<String> iter = gCode.descendingIterator();
        while (iter.hasNext()) {
            double e = readDouble(E_PATTERN, iter.next());

            if (e != -1) {
                endE = e;
                break;
            }
        }

        if (hasRetraction(false, printer)) {
            return endE;
        } else {
            write(out, "G1 F%f E%.5f%n", printer.retractionSpeed, endE - printer.retractionAmount);

            // Reset to travel speed
            write(out, "G0 F%f%n", printer.travelSpeed * 60);

            return endE - printer.retractionAmount;
        }
    }

    private boolean hasRetraction(boolean onStart, Printer printer) {
        double e = -1;

        Iterator<String> iter = onStart ? gCode.iterator() : gCode.descendingIterator();

        int i = 0;
        while (iter.hasNext()) {
            if (i++ >= 20) {
                break;
            }

            double tE = readDouble(E_PATTERN, iter.next());
            if (tE != -1) {
                if (e == -1) {
                    e = tE;
                } else return (tE - e) * (onStart ? 1 : -1) == printer.retractionAmount;
            }
        }

        return false;
    }

    @Override
    public List<GCInstruction> getInstructions() {
        return null;
    }
}
