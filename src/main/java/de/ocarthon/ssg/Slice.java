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

    public static void main(String[] args) throws Exception {
        File file = new File("Kugel.stl");
        Object3D obj = ObjectReader.readObject(file);
        obj.centerObject();

        CuraEngine engine = new CuraEngine(7788);
        engine.addListener(Slice::writeSliceProgress);

        Printer printer = Printer.k8400();
        printer.useDualPrint = false;
        printer.useSupport = true;

        engine.slice(printer, obj);
    }

    public static void writeSliceProgress(SliceProgress p) {
        File file = new File("Bogen.gcode");
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
