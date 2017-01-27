package de.ocarthon.ssg.gcode;

import de.ocarthon.libArcus.Error;
import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.curaengine.CuraEngine;
import de.ocarthon.ssg.curaengine.CuraEngineListener;
import de.ocarthon.ssg.curaengine.SliceProgress;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.Object3D;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Splicer {
    static final Pattern Z_PATTERN = Pattern.compile("Z(\\d*\\.*\\d*)");
    static final Pattern E_PATTERN = Pattern.compile("E(\\d*\\.*\\d*)");

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
        ce1.addListener(new CuraEngineListener() {
            @Override
            public void onSliceStart(SliceProgress p) {
                System.out.println("START");
                sliceResult[0] = p;
            }

            @Override
            public void onProgressUpdate(SliceProgress p) {
                System.out.println(p.getProgress());
            }

            @Override
            public void onError(SliceProgress p, Error e) {
                System.out.println(e.getErrorMessage());
            }

            @Override
            public void onSliceFinished(SliceProgress p) {
                System.out.println("FINISHED");
            }
        });
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

    public static void addSupportLayersToGObj(GCObject obj, SliceProgress slice, double supportMinHeight, Printer printer) throws UnsupportedEncodingException {
        boolean retraction = printer.retraction && printer.retractionAmount != 0;

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
                        String[] l2 = layers.get(i1-1).getData().toString("UTF-8").split("\n");
                        for (int j = l2.length-1; j > 0; j--) {
                            e = readDouble(E_PATTERN, l2[j]);
                            if (e != -1) {
                                break;
                            }
                        }
                    }

                    supportLines.add(lines[i]);
                    supportLines.add("G92 E" + e);
                    supportLines.add(lines[i-1] + " Z" + z);

                    continue;
                } else if (lines[i].startsWith(";TYPE") && support == 1) {
                    break;
                }

                if (support == 1) {
                    supportLines.add(lines[i]);
                }
            }

            obj.newGLayer(supportLines, z, printer.getExtruder(0).layerHeight, printer.getExtruder(0));
        }
    }

    public static void splice(SliceProgress slice, GCObject obj, Printer printer, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);

        // Start GCode
        fos.write((printer.startGcode + "\n").getBytes());

        LinkedList<Cura.GCodeLayer> layers = slice.getLayers();
        double lastE = 0;

        int objLayer = 0;
        int layNr = 0;

        boolean firstLayer = true;

        for (Iterator<Cura.GCodeLayer> iterator = layers.iterator(); iterator.hasNext(); ) {
            layNr++;
            Cura.GCodeLayer layer = iterator.next();

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
                    fos.write(("G1 F1500 E" + e + "\n").getBytes());
                }

                lastE = e;

                continue;
            }

            while (obj.layerCount() > objLayer && obj.getLayer(objLayer).getOffset() <= z) {
                GCLayer gcLayer = obj.getLayer(objLayer);
                gcLayer.calculateValues(printer);

                fos.write("; GC_LAYER\n".getBytes());
                gcLayer.writeGCode(fos, printer);
                objLayer++;
            }

            if (printer.retractionEnabled()) {
                int j = 0;
                int end = 0;

                if (!startsWithRetraction(lines, printer)) {
                    boolean retract = true;
                    while (!lines[j].startsWith("G1")) {
                        fos.write((lines[j++]+"\n").getBytes());

                        if (j >= lines.length) {
                            retract = false;
                            break;
                        }
                    }

                    if (retract) {
                        fos.write(("G92 E" + (lastE - printer.retractionAmount) + "\n").getBytes());
                        fos.write(("G1 F1500 E" + lastE + "\n").getBytes());

                        fos.write(lines[j++].getBytes());
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
                            fos.write((lines[l] + "\n").getBytes());
                        }
                    } else {
                        for (int l = j; l <= end; l++) {
                            fos.write((lines[l] + "\n").getBytes());
                        }

                        fos.write("G92 E0\n".getBytes());
                        fos.write(("G1 F1400 E-" + printer.retractionAmount).getBytes());

                        for (int l = end + 1; l < lines.length; l++) {
                            fos.write((lines[l] + "\n").getBytes());
                        }
                    }
                } else {
                    for (int l = j; l < lines.length; l++) {
                        fos.write((lines[l] + "\n").getBytes());
                    }
                }
            } else {
                fos.write(("G92 E" + lastE + "\n").getBytes("UTF-8"));
                fos.write(layer.getData().toByteArray());

            }

            lastE = e;
        }

/*        for (int i = 0; i < layers.size(); i++) {
            Cura.GCodeLayer layer = layers.get(i);
            String[] lines = layer.getData().toString("UTF-8").split("\n");

            double z = -1;
            for (String line : lines) {
                z = readDouble(Z_PATTERN, line);
                if (z != -1) {
                    break;
                }
            }

            double e = -1;
            for (int j = lines.length - 1; j > 0; j--) {
                e = readDouble(E_PATTERN, lines[j]);
                if (e != -1) {
                    break;
                }
            }

            while (obj.layerCount() > objLayer && obj.getLayer(objLayer).getOffset() <= z) {
                GCLayer gcLayer = obj.getLayer(objLayer);
                gcLayer.calculateValues(printer);
                fos.write("; GC_LAYER\n".getBytes("UTF-8"));

                gcLayer.writeGCode(fos, printer);
                objLayer++;
            }

            fos.write(("G92 E" + lastE + "\n").getBytes("UTF-8"));
            fos.write(layer.getData().toByteArray());

            lastE = e;
        }

        */

        fos.write((printer.endGcode+"\n").getBytes("UTF-8"));
        fos.flush();
        fos.close();
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

        for (int i = lines.length-1; i >= Math.max(lines.length - 10, 0) ; i--) {
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
}
