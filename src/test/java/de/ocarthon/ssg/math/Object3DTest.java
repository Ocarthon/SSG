package de.ocarthon.ssg.math;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class Object3DTest {
    private Object3D object;

    @Before
    public void setUp() throws Exception {
        object = new Object3D(12);

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
    }

    @Test
    public void centerObject() throws Exception {
        object.centerObject();
        System.out.println(object);
        Facet g = object.facets.get(0);
        assertEquals(-5, g.p1.x, 0.001);
        assertEquals(5, g.p1.y, 0.001);
        assertEquals(10, g.p1.z, 0.001);

        double min = Double.MAX_VALUE;

        for (Facet f : object.facets) {
            min = Math.min(min, f.findLowestZ());
        }

        assertEquals(0, min, 0.0001);
    }

}