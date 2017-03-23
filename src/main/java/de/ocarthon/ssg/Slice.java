package de.ocarthon.ssg;

import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.curaengine.CuraEngine;
import de.ocarthon.ssg.curaengine.SliceProgress;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.math.Object3D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Slice {
    public static String outName;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Invalid args");
            return;
        }

        File file = new File(args[0]);
        outName = args[1];

        Object3D obj = ObjectReader.readObject(file);
        obj.centerObject();

        CuraEngine engine = new CuraEngine(7788);
        engine.addListener(Slice::writeSliceProgress);

        Printer printer = Printer.k8400();
        printer.useDualPrint = false;

        if (args.length == 3) {
            printer.useSupport = Boolean.valueOf(args[2]);
        } else {
            printer.useSupport = true;
        }

        engine.slice(printer, obj);
    }

    private static void writeSliceProgress(SliceProgress sliceProgress) {
        writeSliceProgress(sliceProgress, outName);
    }

    public static void writeSliceProgress(SliceProgress p, String fileName) {
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            for (Cura.GCodeLayer layer : p.getLayers()) {
                fos.write(";LAYER\n".getBytes());
                fos.write(layer.toByteArray());
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
