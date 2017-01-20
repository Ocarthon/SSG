package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GCObject {
    private List<GCLayer> layers;

    public GCObject() {
        layers = new ArrayList<>();
    }

    public GCLayer newLayer(double offset, double layerHeight, Extruder extruder) {
        GCLayer layer = new GCLayer(offset, layerHeight, extruder);
        addLayer(layer);
        return layer;
    }

    public GCLayer newGLayer(List<String> lines, double offset, double layerHeight, Extruder extruder) {
        GCCLayer layer = new GCCLayer(lines, offset, layerHeight, extruder);
        addLayer(layer);
        return layer;
    }

    public void addLayer(GCLayer layer) {
        layers.add(layer);
        layers.sort((o1, o2) -> o1.getOffset() < o2.getOffset() ? -1 : o1.getOffset() == o2.getOffset() ? o1.getExtruder().extruderNr - o2.getExtruder().extruderNr : 1);
    }

    public int layerCount() {
        return layers.size();
    }

    public GCLayer getLayer(int n) {
        return layers.get(n);
    }

    public double totalHeight() {
        GCLayer last = layers.get(layers.size()-1);
        return last.getOffset();
    }

    public void exportLayer(OutputStream out, Printer printer) {

    }

    public void exportInstructions(OutputStream out, Printer printer) {
        PrintWriter writer = new PrintWriter(out);
        double e = 0;
        for (GCLayer layer : layers) {
            e = layer.calculateValues(printer, e);
            for (GCInstruction instruction : layer.getInstructions()) {
                writer.format("%s%n", instruction.convertToGCode(printer, layer.getExtruder()));
            }
        }
        writer.flush();
    }
}
