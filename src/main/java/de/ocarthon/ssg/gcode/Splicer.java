package de.ocarthon.ssg.gcode;

import static de.ocarthon.ssg.util.FileUtil.write;

import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.curaengine.CuraEngine;
import de.ocarthon.ssg.curaengine.SliceProgress;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.MathUtil;
import de.ocarthon.ssg.math.Object3D;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Splicer {
    private static final boolean WRITE_MESSAGES = true;

    static final Pattern X_PATTERN = Pattern.compile("X(\\d*\\.*\\d*)");
    static final Pattern Y_PATTERN = Pattern.compile("Y(\\d*\\.*\\d*)");
    static final Pattern Z_PATTERN = Pattern.compile("Z(\\d*\\.*\\d*)");
    static final Pattern E_PATTERN = Pattern.compile("E(-*\\d*\\.*\\d*)");

    public static void sliceAndSplice(Object3D object3D, GCObject obj, double supportMinHeight, Printer printer, File file) throws IOException, InterruptedException {
        final SliceProgress[] sliceResult = new SliceProgress[1];
        CuraEngine ce1 = new CuraEngine(7777);
        ce1.addListener(p -> sliceResult[0] = p);
        ce1.slice(printer, object3D);
        while (sliceResult[0] == null || sliceResult[0].getProgress() != 1 || ce1.isProcessRunning()) {
            Thread.sleep(1000);
        }

        SliceProgress sliceMain = sliceResult[0];
        sliceResult[0] = null;

        ce1.reset();
        printer.useSupport = true;
        ce1.slice(printer, object3D);
        printer.useSupport = false;
        while (sliceResult[0] == null || sliceResult[0].getProgress() != 1) {
            Thread.sleep(1000);
        }

        splice(sliceMain, sliceResult[0], supportMinHeight, obj, printer, file);
    }

    public static void splice(SliceProgress sliceMain, SliceProgress sliceWSup, double supportMinHeight, GCObject obj, Printer printer, File file) throws IOException {
        addLayersToGObj(obj, sliceMain, printer.getExtruder(0));
        addSupportLayersToGObj(obj, sliceWSup, supportMinHeight, printer, printer.getExtruder(printer.useDualPrint ? 1 : 0));

        splice(obj, printer, file);
    }

    private static void splice(GCObject obj, Printer printer, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);

        Extruder mainExt = printer.getExtruder(0);
        Extruder secExt = printer.getExtruder(1);
        Extruder actExt = mainExt;

        List<GCLayer> actExtLayers = new ArrayList<>(2);
        List<GCLayer> inactExtLayers = new ArrayList<>(2);

        PrimeTower primeTower = new PrimeTower(printer, false);

        List<List<GCLayer>> layersByOffset = groupLayersByOffset(obj.getLayers());


        double e = writeStartCommands(fos, printer);;


        int layerCount = layersByOffset.size();
        int currentLayer = 0;
        for (List<GCLayer> layers : layersByOffset) {
            writeComment(fos, "Layer " + ++currentLayer + "/" + layerCount);

            // Sort layers into their corresponding lists
            actExtLayers.clear();
            inactExtLayers.clear();

            for (GCLayer layer : layers) {
                if (!layer.hasContent()) {
                    continue;
                }

                if (layer.getExtruder() == actExt) {
                    actExtLayers.add(layer);
                } else {
                    inactExtLayers.add(layer);
                }
            }

            // Write layers for currently active extruder
            for (GCLayer layer : actExtLayers) {
                e = layer.writeGCode(fos, e, printer);
            }


            if (!hasContent(inactExtLayers)) {
                if (printer.usePrimeTower && printer.useDualPrint) {
                    // prime
                    e = primeTower.printLayer(fos, e, actExt);
                }
                continue;
            }

            // Switch extruder
            writeComment(fos, "Switching extruder");
            e = switchExtruder(fos, e, primeTower, printer, actExt == mainExt ? secExt : mainExt);
            actExt = actExt == mainExt ? secExt : mainExt;

            // Write layers for currently active extruder
            for (GCLayer layer : inactExtLayers) {
                e = layer.writeGCode(fos, e, printer);
            }
        }

        writeEndCommands(fos, printer);
    }

    private static boolean hasContent(List<GCLayer> layers) {
        for (GCLayer layer : layers) {
            if (layer.hasContent()) {
                return true;
            }
        }

        return false;
    }

    private static double switchExtruder(OutputStream out, double e, PrimeTower primeTower, Printer printer, Extruder inactExt) throws IOException {
        // Move to nozzle switch position
        write(out, "G0 F%f X%.5f Y%.5f%n", printer.travelSpeed * 60, printer.nozzleSwitchPosition.x, printer.nozzleSwitchPosition.y);

        // Retract
        write(out, "G1 F%f E%.5f%n", printer.nozzleSwitchRetractionSpeed, e - printer.nozzleSwitchRetractionAmount + printer.retractionAmount);

        // Switch extruder
        write(out, "T%d%n", inactExt.extruderNr);
        write(out, "G92 E0%n");

        write(out, "G1 F%f E%.5f%n", printer.nozzleSwitchRetractionSpeed, printer.nozzleSwitchRetractionAmount - printer.retractionAmount);

        return primeTower.printLayer(out, printer.nozzleSwitchRetractionAmount - printer.retractionAmount, inactExt);
    }

    private static double writeStartCommands(FileOutputStream fos, Printer printer) throws IOException {
        Extruder mainExt = printer.getExtruder(0);
        Extruder secExt = printer.getExtruder(1);

        writeComment(fos, "Heating extruder(s)");

        // Heat to intermediate temperature
        write(fos, "M104 T0 S%f%n", mainExt.intermediateTemperature);

        if (printer.useDualPrint) {
            write(fos, "M109 T1 S%f%n", secExt.intermediateTemperature);
        }

        write(fos, "M109 T0 S%f%n", mainExt.intermediateTemperature);


        // Set printing temperature as target
        write(fos, "M104 T0 S%f%n", mainExt.printTemperature);

        if (printer.useDualPrint) {
            write(fos, "M104 T1 S%f%n", secExt.printTemperature);
        }

        // Start GCode
        writeComment(fos, "Starting...");
        write(fos, "%s%n", printer.startGCode);

        // Initial priming
        writeComment(fos, "Initial priming");
        PrimeTower initPrimeTower = new PrimeTower(printer, true);

        if (printer.useDualPrint) {
            write(fos, "T1%n");
            write(fos, "G92 E0%n");
            double e = initPrimeTower.printLayer(fos, 0, secExt);

            write(fos, "G1 F%f E%.5f%n", printer.nozzleSwitchRetractionSpeed, e - printer.nozzleSwitchRetractionAmount + printer.retractionAmount);}

        // Extruder 0
        write(fos, "T0%n");
        write(fos, "G92 E0%n");
        return initPrimeTower.printLayer(fos, 0, mainExt);
    }

    private static void writeEndCommands(FileOutputStream fos, Printer printer) throws IOException {
        // End GCode
        write(fos, printer.endGCode);

        // Set target temperature to 0
        write(fos, "M104 T0 S0%n");

        if (printer.useDualPrint) {
            write(fos, "M104 T1 S0%n");
        }
    }

    private static List<List<GCLayer>> groupLayersByOffset(List<GCLayer> layers) {
        List<List<GCLayer>> layersByOffset = new LinkedList<>();

        List<GCLayer> currentLayer = new ArrayList<>(2);
        double currentHeight = layers.get(0).getOffset();

        currentLayer.add(layers.get(0));

        for (int i = 1; i < layers.size(); i++) {
            GCLayer layer = layers.get(i);

            if (MathUtil.equals(layer.getOffset(), currentHeight)) {
                currentLayer.add(layer);
            } else {
                currentHeight = layers.get(i).getOffset();
                layersByOffset.add(currentLayer);
                currentLayer = new ArrayList<>(2);
                currentLayer.add(layer);
            }
        }

        return layersByOffset;
    }

    private static void addLayersToGObj(GCObject obj, SliceProgress slice, Extruder extruder) {
        LinkedList<Cura.GCodeLayer> layers = slice.getLayers();
        double lastE = 0;

        for (Cura.GCodeLayer layer : layers) {
            String sLayer = layer.getData().toString(StandardCharsets.UTF_8);
            double z = readDouble(Z_PATTERN, sLayer);

            if (z == -1) {
                continue;
            }

            LinkedList<String> lineList = new LinkedList<>();
            String[] lines = sLayer.split("\n");

            Collections.addAll(lineList, lines);

            obj.newGLayer(lineList, z, extruder.layerHeight, lastE, extruder);

            Iterator<String> iter = lineList.descendingIterator();
            while (iter.hasNext()) {
                double e = readDouble(E_PATTERN, iter.next());

                if (e != -1) {
                    lastE = e;
                    break;
                }
            }
        }
    }

    private static void addSupportLayersToGObj(GCObject obj, SliceProgress slice, double supportMinHeight, Printer printer, Extruder ext) throws UnsupportedEncodingException {
        LinkedList<Cura.GCodeLayer> layers = slice.getLayers();
        for (int i1 = 0; i1 < layers.size(); i1++) {
            Cura.GCodeLayer layer = layers.get(i1);
            String sLayer = layer.getData().toString("UTF-8");
            double z = readDouble(Z_PATTERN, sLayer);
            if (z < supportMinHeight) {
                continue;
            }

            LinkedList<String> supportLines = new LinkedList<>();
            String[] lines = sLayer.split("\n");
            int support = 0;
            double e = -1;
            boolean firstG1 = false;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].startsWith(";TYPE:SUPPORT")) {
                    support = 1;

                    // Search last e
                    /*for (int j = i - 1; j > 0; j--) {
                        e = readDouble(E_PATTERN, lines[j]);
                        if (e != -1) {
                            break;
                        }
                    }

                    if (e == -1) {
                        String[] l2 = layers.get(i1 - 1).getData().toString("UTF-8").split("\n");
                        for (int j = l2.length - 1; j > 0; j--) {
                            e = readDouble(E_PATTERN, l2[j]);
                            if (e != -1) {
                                break;
                            }
                        }
                    }*/

                    // Search first e, as this will be a retraction move

                    supportLines.add(applyOffset(lines[i], ext));
                    supportLines.add(applyOffset(lines[i - 1], ext) + " Z" + z);

                    continue;
                } else if (lines[i].startsWith(";TYPE") && support == 1) {
                    break;
                } else if (lines[i].startsWith("G1") && !firstG1) {
                    firstG1 = true;
                    e = readDouble(E_PATTERN, lines[i]) + printer.retractionAmount;
                    continue;
                }

                if (support == 1) {
                    supportLines.add(applyOffset(lines[i], ext));
                }
            }


            obj.newGLayer(supportLines, z, ext.layerHeight, e, ext);
        }
    }

    private static String applyOffset(String line, Extruder extruder) {
        if (!line.startsWith("G1") && !line.startsWith("G0") && !line.startsWith("G2")) {
            return line;
        }

        if (extruder.nozzleOffsetX == 0 && extruder.nozzleOffsetY == 0) {
            return line;
        }

        double x = readDouble(X_PATTERN, line) + extruder.nozzleOffsetX;
        double y = readDouble(Y_PATTERN, line) + extruder.nozzleOffsetY;

        line = X_PATTERN.matcher(line).replaceFirst(String.format("X%.5f", x));
        line = Y_PATTERN.matcher(line).replaceFirst(String.format("Y%.5f", y));

        return line;
    }

    static double readDouble(Pattern pattern, String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            String s = m.group().trim().substring(1);
            if (s.length() == 0) {
                return -1;
            } else {
                return Double.parseDouble(s);
            }
        } else {
            return -1;
        }
    }

    private static void writeComment(OutputStream out, String comment) throws IOException {
        write(out, "; %s%n", comment);

        if (WRITE_MESSAGES) {
            write(out, "M117 %s%n", comment);
        }
    }
}
