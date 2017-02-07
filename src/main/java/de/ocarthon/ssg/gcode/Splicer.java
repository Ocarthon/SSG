package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.curaengine.CuraEngine;
import de.ocarthon.ssg.curaengine.SliceProgress;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.math.Object3D;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Splicer {
    static final Pattern X_PATTERN = Pattern.compile("X(\\d*\\.*\\d*)");
    static final Pattern Y_PATTERN = Pattern.compile("Y(\\d*\\.*\\d*)");
    static final Pattern Z_PATTERN = Pattern.compile("Z(\\d*\\.*\\d*)");
    static final Pattern E_PATTERN = Pattern.compile("E(-*\\d*\\.*\\d*)");

    public static void main(String[] args) throws Exception {
        Object3D obj = ObjectReader.readObject(new File("Bogen.stl"));
        obj.centerObject();

        Printer printer = new Printer();
        printer.addExtruder(new Extruder());

        GCObject gcobj = new GCObject();

        sliceAndSplice(obj, gcobj, 10, printer, new File("test2.gcode"));
    }

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
        addSupportLayersToGObj(obj, sliceWSup, supportMinHeight, printer);

        splice(sliceMain, obj, printer, file);
    }

    public static void write(SliceProgress p, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);

        for (Cura.GCodeLayer layer : p.getLayers()) {
            fos.write(layer.toByteArray());
        }

        fos.flush();
        fos.close();
    }

    public static void splice(SliceProgress slice, GCObject obj, Printer printer, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);

        LinkedList<Cura.GCodeLayer> layers = slice.getLayers();

        Extruder mainExt = printer.getExtruder(0);
        Extruder activeExt = mainExt;
        PrimeTower primeTower = new PrimeTower(printer);

        // Heat up extruder
        if (printer.useDualPrint) {
            write(fos, "M104 T1 S%f%n", printer.getExtruder(1).standbyTemperature);
        }

        write(fos, "M109 T0 S%f%n", mainExt.printTemperature);

        // Start GCode
        write(fos, "%s%n", printer.startGCode);


        double lastE = 0;
        int objLayer = 0;

        boolean firstLayer = true;

        for (Cura.GCodeLayer layer : layers) {
            // Current layer split into lines
            String[] lines = layer.getData().toString("UTF-8").split("\n");

            // z-detection
            double z = -1;
            for (String line : lines) {
                z = readDouble(Z_PATTERN, line);
                if (z != -1) {
                    break;
                }
            }

            // e-detection
            double e = -1;
            for (int j = lines.length - 1; j > 0; j--) {
                e = readDouble(E_PATTERN, lines[j]);
                if (e != -1) {
                    break;
                }
            }

            // Always print the first layer
            // The first layer always start with a retraction move
            if (firstLayer) {
                firstLayer = false;
                fos.write(layer.getData().toByteArray());

                if (!endsWithRetraction(lines, printer)) {
                    e -= printer.retractionAmount;
                    write(fos, "G1 F1500 E%.5f%n", e);
                }

                lastE = e;

                continue;
            }

            while (obj.layerCount() > objLayer && obj.getLayer(objLayer).getOffset() <= z) {
                GCLayer gcLayer = obj.getLayer(objLayer);

                if (gcLayer.getInstructions() == null || gcLayer.getInstructions().size() != 0) {
                    write(fos, "; GC_LAYER%n");
                    Extruder ext = gcLayer.getExtruder();
                    if (activeExt != ext) {
                        performExtruderChange(fos, printer, activeExt, ext, primeTower, z);
                        activeExt = ext;
                    }
                    gcLayer.writeGCode(fos, printer);
                }

                objLayer++;
            }

            if (activeExt != mainExt) {
                performExtruderChange(fos, printer, activeExt, mainExt, primeTower, z);
                activeExt = mainExt;
            }

            int j = 0;
            int end;

            if (!startsWithRetraction(lines, printer)) {
                boolean retract = true;
                while (!lines[j].startsWith("G1")) {
                    write(fos, "%s%n", lines[j++]);

                    if (j >= lines.length) {
                        retract = false;
                        break;
                    }
                }

                if (retract) {
                    write(fos, "G92 E%.5f%n", lastE - printer.retractionAmount);
                    write(fos, "G1 F1500 E%.5f%n", lastE);

                    write(fos, "%s%n", lines[j++]);
                }
            }

            if (!endsWithRetraction(lines, printer)) {
                end = lines.length - 1;
                while (!lines[end].startsWith("G1")) {
                    end--;

                    if (end < 0) {
                        break;
                    }
                }

                if (end < 0) {
                    for (int l = j; l < lines.length; l++) {
                        write(fos, "%s%n", lines[l]);
                    }
                } else {
                    for (int l = j; l <= end; l++) {
                        write(fos, "%s%n", lines[l]);
                    }

                    write(fos, "G92 E%.5f%n", printer.retractionAmount);
                    write(fos, "G1 F1500 E0%n");

                    for (int l = end + 1; l < lines.length; l++) {
                        write(fos, "%s%n", lines[l]);
                    }
                }
            } else {
                for (int l = j; l < lines.length; l++) {
                    write(fos, "%s%n", lines[l]);
                }
            }

            lastE = e;
        }

        write(fos, "%s%n", printer.endGCode);

        write(fos, "M104 T0 S0%n");

        if (printer.useDualPrint) {
            write(fos, "M104 T1 S0%n");
        }

        fos.flush();
        fos.close();
    }

    public static void addSupportLayersToGObj(GCObject obj, SliceProgress slice, double supportMinHeight, Printer printer) throws UnsupportedEncodingException {
        Extruder ext = printer.getExtruder(printer.useDualPrint ? 1 : 0);

        LinkedList<Cura.GCodeLayer> layers = slice.getLayers();
        for (int i1 = 0; i1 < layers.size(); i1++) {
            Cura.GCodeLayer layer = layers.get(i1);
            String sLayer = layer.getData().toString("UTF-8");
            double z = readDouble(Z_PATTERN, sLayer);
            if (z < supportMinHeight) {
                continue;
            }

            List<String> supportLines = new LinkedList<>();
            String[] lines = sLayer.split("\n");
            int support = 0;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].startsWith(";TYPE:SUPPORT")) {
                    support = 1;


                    double e = -1;
                    // Search last e
                    for (int j = i - 2; j > 0; j--) {
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
                    }

                    supportLines.add(applyOffset(lines[i], ext));
                    supportLines.add("G92 E" + e);
                    supportLines.add(applyOffset(lines[i - 1], ext) + " Z" + z);

                    continue;
                } else if (lines[i].startsWith(";TYPE") && support == 1) {
                    break;
                }

                if (support == 1) {
                    supportLines.add(applyOffset(lines[i], ext));
                }
            }


            obj.newGLayer(supportLines, z, ext.layerHeight, ext);
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

    private static void performExtruderChange(OutputStream out, Printer printer, Extruder off, Extruder on, PrimeTower primeTower, double z) throws IOException {
        write(out, "; EXTRUDER CHANGE%n");

        // Move to (0, 0) to wait for temperature
        write(out, "G0 F%.5f X%.5f Y%.5f%n", printer.travelSpeed * 60, printer.origin.x, printer.origin.y);

        // bring inactive extruder down to standby temperature
        write(out, "M104 T%d S%f%n", off.extruderNr, off.standbyTemperature);

        // heat active extruder and wait
        write(out, "M109 T%d S%f%n", on.extruderNr, on.printTemperature);

        write(out, "T%d%n", on.extruderNr);

        if (printer.usePrimeTower || !on.isPrimed) {
            // Print prime tower
            primeTower.printLayer(out, printer, on);

            // Reset e-axis
            write(out, "G92 E0%n");
        }

        // move to given z + layer height to be safe
        write(out, "G1 F%f Z%.5f%n", printer.travelSpeed * 60, z + printer.layerHeight);
    }

    private static boolean startsWithRetraction(String[] lines, Printer printer) {
        double e = -1;

        for (int i = 0; i < Math.min(lines.length, 10); i++) {
            String line = lines[i];
            double tE = readDouble(E_PATTERN, line);
            if (tE != -1) {
                if (e == -1) {
                    e = tE;
                } else return tE - e == printer.retractionAmount;
            }
        }

        return false;
    }

    private static boolean endsWithRetraction(String[] lines, Printer printer) {
        double e = -1;

        for (int i = lines.length - 1; i >= Math.max(lines.length - 10, 0); i--) {
            double tE = readDouble(E_PATTERN, lines[i]);
            if (tE != -1) {
                if (e == -1) {
                    e = tE;
                } else return e - tE == printer.retractionAmount;
            }
        }

        return false;
    }

    private static double readDouble(Pattern pattern, String line) {
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

    private static void write(OutputStream out, String format, Object... args) throws IOException {
        out.write(String.format(format, args).getBytes("UTF-8"));
    }
}
