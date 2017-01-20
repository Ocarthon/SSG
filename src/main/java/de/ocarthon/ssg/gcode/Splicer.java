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
import java.util.LinkedList;
import java.util.List;
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
        final SliceProgress[] sliceMain = new SliceProgress[1];
        CuraEngine ce1 = new CuraEngine(7777);
        ce1.addListener(new CuraEngineListener() {
            @Override
            public void onSliceStart(SliceProgress p) {
                System.out.println("START");
                sliceMain[0] = p;
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
        while (sliceMain[0] == null || sliceMain[0].getProgress() != 1 || ce1.isProcessRunning()) {
            Thread.sleep(1000);
        }

        final SliceProgress[] sliceWSup = new SliceProgress[1];
        CuraEngine ce2 = new CuraEngine(7777);
        ce2.addListener(new CuraEngineListener() {
            @Override
            public void onSliceStart(SliceProgress p) {
                System.out.println("START");
                sliceWSup[0] = p;
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
        printer.useSupport = true;
        ce2.slice(printer, object3D);
        printer.useSupport = false;
        while (sliceWSup[0] == null || sliceWSup[0].getProgress() != 1) {
            Thread.sleep(1000);
        }


        write(sliceWSup[0], new File("sup.gcode"));

        splice(sliceMain[0], sliceWSup[0], supportMinHeight, obj, printer, file);
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

                    supportLines.add("G92 E" + e + "\n" + lines[i - 1] + "\nG0 F7200 Z" + z + "\n");
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

        fos.write((printer.startGcode+"\n").getBytes("UTF-8"));

        int i = 0;
        int objLayer = 0;
        double lastE = 0;
        for (Cura.GCodeLayer layer : slice.getLayers()) {
            String[] lines = layer.getData().toString("UTF-8").split("\n");

            double z = -1;
            for (String line : lines) {
                z = readDouble(Z_PATTERN, line);
                if (z != -1) {
                    break;
                }
            }

            double e = -1;
            for (int j = lines.length-1; j > 0; j--) {
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

        fos.write((printer.endGcode+"\n").getBytes("UTF-8"));
        fos.flush();
        fos.close();
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
