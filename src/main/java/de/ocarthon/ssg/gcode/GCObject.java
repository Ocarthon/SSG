package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.util.FileUtil;

import java.io.IOException;
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
        sort();
    }

    private void sort() {
        layers.sort((o1, o2) -> o1.getOffset() < o2.getOffset() ? -1 : o1.getOffset() == o2.getOffset() ? (o1.getClass() == GCLayer.class ? 1 : -1) : 1);
    }

    public void merge(GCObject obj) {
        layers.addAll(obj.getLayers());

        layers.removeIf(gcLayer -> !gcLayer.hasContent());

        sort();
    }

    public List<GCLayer> getLayers() {
        return layers;
    }

    public void exportInstructions(OutputStream out, Printer printer) throws IOException {
        double e = 0;
        for (GCLayer layer : layers) {
            e = layer.calculateValues(printer, e);
            for (GCInstruction instruction : layer.getInstructions()) {
                FileUtil.write(out, "%s%n", instruction.convertToGCode(printer, layer.getExtruder()));
            }
        }

        out.flush();
    }
}
