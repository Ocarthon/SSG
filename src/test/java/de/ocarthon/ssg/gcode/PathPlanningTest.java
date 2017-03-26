package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.Vector;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class PathPlanningTest {
    GCObject obj;

    @Before
    public void setUp() throws Exception {
        obj = new GCObject();

        Printer printer = new Printer();
        Extruder ext = new Extruder();
        printer.addExtruder(ext);


        for (int i = 1; i < 10; i++) {
            GCLayer layer = obj.newLayer(0.2, 0.2, ext);

            if (i % 2 == 0) {
                layer.add(new GCInstructions.G0(10 * i, 10));
                layer.add(new GCInstructions.G0(10 * i, 30));
            } else {
                layer.add(new GCInstructions.G0(10 * i, 30));
                layer.add(new GCInstructions.G0(10 * i, 10));
            }
        }

    }

    @Test
    public void searchBestPath() throws Exception {
        Vector end = PathPlanning.searchBestPath(new Vector(0, 10, 0), obj.getLayers());

        Collections.shuffle(obj.getLayers());

        Vector end2 = PathPlanning.searchBestPath(new Vector(0, 10, 0), obj.getLayers());

        assertEquals(end, end2);

    }

}