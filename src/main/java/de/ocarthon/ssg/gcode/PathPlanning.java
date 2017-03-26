package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.math.Vector;

import java.util.*;

public class PathPlanning {

    public static Vector searchBestPath(Vector beginning, List<GCLayer> layers) {
        // Maps to store begin and end point of all layers
        HashMap<GCLayer, Vector> beginnings = new HashMap<>(layers.size());
        HashMap<GCLayer, Vector> endings = new HashMap<>(layers.size());

        // Find begin and end points of layers
        for (GCLayer layer : layers) {
            findBeginningAndEnd(layer, beginnings, endings);
        }

        Vector position = beginning;

        // Index that is currently searched
        for (int i = 0; i < layers.size() - 1; i++) {
            double minDst2 = Double.MAX_VALUE;
            int index = -1;

            // Start at i, as all before that are already sorted
            for (int j = i; j < layers.size(); j++) {
                double dst2 = Vector.dst2XY(position, beginnings.get(layers.get(j)));

                if (dst2 < minDst2) {
                    minDst2 = dst2;
                    index = j;
                }
            }

            // Update current position to end position
            position = endings.get(layers.get(index));

            // Swap entries in list
            swap(layers, i, index);
        }

        // Return end position of last layer
        return endings.get(layers.get(layers.size() - 1));
    }

    private static <T> void swap(List<T> list, int i1, int i2) {
        T temp = list.get(i1);
        list.set(i1, list.get(i2));
        list.set(i2, temp);
    }

    private static void findBeginningAndEnd(GCLayer layer, Map<GCLayer, Vector> beginning, Map<GCLayer, Vector> endings) {
        if (layer instanceof GCCLayer) {
            GCCLayer gLayer = ((GCCLayer) layer);

            for (String line : gLayer.gCode) {
                if (!line.startsWith("G0") && !line.startsWith("G1")) {
                    continue;
                }

                beginning.put(layer, new Vector(GCUtil.readDouble(GCUtil.X_PATTERN, line), GCUtil.readDouble(GCUtil.Y_PATTERN, line), 0));
                break;
            }

            Iterator<String> iter = gLayer.gCode.descendingIterator();

            while (iter.hasNext()) {
                String line = iter.next();

                if (!line.startsWith("G0") && !line.startsWith("G1")) {
                    continue;
                }

                endings.put(layer, new Vector(GCUtil.readDouble(GCUtil.X_PATTERN, line), GCUtil.readDouble(GCUtil.Y_PATTERN, line), 0));
                break;
            }
        } else {
            List<GCInstruction> instructions = layer.getInstructions();

            for (GCInstruction instruction : instructions) {
                if (!(instruction instanceof GCInstructions.G0) || instruction instanceof GCInstructions.G2) {
                    continue;
                }

                GCInstructions.G0 g0 = ((GCInstructions.G0) instruction);

                beginning.put(layer, new Vector(g0.x, g0.y, 0));
                break;
            }

            for (int i = instructions.size() - 1; i >= instructions.size() - 10; i++) {
                GCInstruction instruction = instructions.get(i);
                if (!(instruction instanceof GCInstructions.G0) || instruction instanceof GCInstructions.G2) {
                    continue;
                }

                GCInstructions.G0 g0 = ((GCInstructions.G0) instruction);

                endings.put(layer, new Vector(g0.x, g0.y, 0));
                break;
            }
        }
    }
}
