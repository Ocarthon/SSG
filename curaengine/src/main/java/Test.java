import de.ocarthon.libArcus.Error;
import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.curaengine.CuraEngine;
import de.ocarthon.ssg.curaengine.CuraEngineListener;
import de.ocarthon.ssg.curaengine.SliceProgress;
import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.math.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class Test {

    public static void main(String[] args) throws IOException, IllegalAccessException {
        Object3D object = new Object3D(12);

        Vector v1 = new Vector(0, 0, 10);
        Vector v2 = new Vector(0, 0, 20);
        Vector v3 = new Vector(0, 10, 10);
        Vector v4 = new Vector(0, 10, 20);
        Vector v5 = new Vector(10, 0, 10);
        Vector v6 = new Vector(10, 0, 20);
        Vector v7 = new Vector(10, 10, 10);
        Vector v8 = new Vector(10, 10, 20);

        object.facets.add(new Facet(v4, v6, v8));
        object.facets.add(new Facet(v6.copy(), v4.copy(), v2));
        object.facets.add(new Facet(v1, v7, v5));
        object.facets.add(new Facet(v7.copy(), v1.copy(), v3));
        object.facets.add(new Facet(v1.copy(), v6.copy(), v2.copy()));
        object.facets.add(new Facet(v6.copy(), v1.copy(), v5.copy()));
        object.facets.add(new Facet(v6.copy(), v7.copy(), v8.copy()));
        object.facets.add(new Facet(v7.copy(), v6.copy(), v5.copy()));
        object.facets.add(new Facet(v7.copy(), v4.copy(), v8.copy()));
        object.facets.add(new Facet(v4.copy(), v7.copy(), v3.copy()));
        object.facets.add(new Facet(v1.copy(), v4.copy(), v3.copy()));
        object.facets.add(new Facet(v4.copy(), v1.copy(), v2.copy()));

        object.centerObject();

        Printer printer = new Printer();
        Extruder ext = new Extruder();
        ext.nozzleOffsetX = 23.7F;
        printer.addExtruder(ext);

        CuraEngine ce = new CuraEngine(7777);
        ce.addListener(new CuraEngineListener() {
            @Override
            public void onSliceStart(SliceProgress p) {
                System.out.println("Start");
            }

            @Override
            public void onProgressUpdate(SliceProgress p) {
                System.out.println("Progress: " + p.getProgress());
            }

            @Override
            public void onError(SliceProgress p, Error e) {
                System.out.println("Error: " + e.getErrorMessage());
            }

            @Override
            public void onSliceFinished(SliceProgress p) {
                System.out.println("Finished");

                File file = new File("test.gcode");

                try {
                    OutputStream out = new FileOutputStream(file);
                    Iterator<Cura.GCodeLayer> iter = p.getLayers().iterator();
                    while (iter.hasNext()) {
                        Cura.GCodeLayer l = iter.next();
                        out.write(l.getData().toByteArray());
                    }
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        ce.slice(printer, object);


    }
}
