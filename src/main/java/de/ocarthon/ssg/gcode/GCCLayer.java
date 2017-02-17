package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class GCCLayer extends GCLayer {
    public List<String> gCode;

    GCCLayer(List<String> gCode, double offset, double layerHeight, Extruder extruder) {
        super(offset, layerHeight, extruder);
        this.gCode = gCode;
    }

    @Override
    public void add(GCInstruction instruction) {
    }

    public boolean hasContent() {
        return gCode.size() != 0;
    }

    @Override
    public double calculateValues(Printer printer, double eOffset) {
        return 0;
    }

    @Override
    public void writeGCode(OutputStream out, Printer printer) throws IOException {
        for (String s : gCode) {
            out.write((s + "\n").getBytes());
        }
    }

    @Override
    public List<GCInstruction> getInstructions() {
        return null;
    }
}
