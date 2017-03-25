package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
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

    public GCLayer newGLayer(LinkedList<String> lines, double offset, double layerHeight, double initialE, Extruder extruder) {
        GCCLayer layer = new GCCLayer(lines, offset, layerHeight, extruder);
        layer.initialE = initialE;
        addLayer(layer);
        return layer;
    }

    public void addLayer(GCLayer layer) {
        layers.add(layer);
        layers.sort((o1, o2) -> o1.getOffset() < o2.getOffset() ? -1 : o1.getOffset() == o2.getOffset() ? (o1.getClass() == GCLayer.class ? 1 : -1) : 1);
    }

    public int layerCount() {
        return layers.size();
    }

    public List<GCLayer> getLayers() {
        return layers;
    }

    public GCLayer getLayer(int n) {
        return layers.get(n);
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
