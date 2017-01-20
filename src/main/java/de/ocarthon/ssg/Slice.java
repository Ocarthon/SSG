package de.ocarthon.ssg;

import de.ocarthon.libArcus.Error;
import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.curaengine.CuraEngine;
import de.ocarthon.ssg.curaengine.CuraEngineListener;
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
        File file = new File("Bogen.stl");
        Object3D obj = ObjectReader.readObject(file);

        CuraEngine engine = new CuraEngine(7788);
        engine.addListener(new CuraEngineListener() {
            @Override
            public void onSliceStart(SliceProgress p) {

            }

            @Override
            public void onProgressUpdate(SliceProgress p) {
                System.out.println(p.getProgress());
            }

            @Override
            public void onError(SliceProgress p, Error e) {

            }

            @Override
            public void onSliceFinished(SliceProgress p) {
                writeSliceProgress(p);
            }
        });

        Printer printer = new Printer();
        printer.supportAngle = 45F;
        printer.useSupport = true;
        Extruder extruder = new Extruder();
        printer.addExtruder(extruder);

        engine.slice(printer, obj);
    }

    public static void writeSliceProgress(SliceProgress p) {
        File file = new File("Bogen_support.gcode");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            for (Cura.GCodeLayer layer : p.getLayers()) {
                fos.write(layer.toByteArray());
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
