package de.ocarthon.ssg.math;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CentroidTest {
    private List<Vector> polygon1;
    private Vector geom1;
    private Vector cheb1;

    private List<Vector> polygon2;
    private Vector geom2;
    private Vector cheb2;

    @Before
    public void setup() {
        polygon1 = new ArrayList<>();
        polygon1.add(new Vector(0, 200, 0));
        polygon1.add(new Vector(200, 0, 0));
        polygon1.add(new Vector(0, 0, 0));
        polygon1.add(new Vector(200, 200, 0));
        geom1 = new Vector(100, 100, 0);
        cheb1 = new Vector(100, 100, 0);

        polygon2 = new ArrayList<>();
        polygon2.add(new Vector(0, 200, 0));
        polygon2.add(new Vector(200, 0, 0));
        polygon2.add(new Vector(0, 0, 0));
        polygon2.add(new Vector(200, 200, 0));
        for (int i = 1; i < 10; i++) {
            polygon2.add(new Vector(i * 20, 0, 0));
        }
        geom2 = new Vector(100, 30.7692307, 0);
        cheb2 = new Vector(100, 100, 0);
    }

    @Test
    public void testGeometricCenter() {
        Vector geom = Centroid.geometricCenter(polygon1);
        assertEquals(geom1, geom);

        geom = Centroid.geometricCenter(polygon2);
        assertEquals(geom2, geom);
    }

    @Test
    public void testChebychevCenter() {
        Vector geom = Centroid.chebychevCenter(polygon1);
        assertEquals(cheb1, geom);

        geom = Centroid.chebychevCenter(polygon2);
        assertEquals(cheb2, geom);
    }

}
