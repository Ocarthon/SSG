package de.ocarthon.ssg.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class VectorTest {
    @Test
    public void copy() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = v1.copy();
        assertEquals(v1, v2);
    }

    @Test
    public void set() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        v1.set(3, 2, 1);

        assertEquals(3, v1.x, 0);
        assertEquals(2, v1.y, 0);
        assertEquals(1, v1.z, 0);
    }

    @Test
    public void add() throws Exception {
        Vector v1 = new Vector(1, 1, 1);
        v1.add(1, 2, 3);

        assertEquals(2, v1.x, 0);
        assertEquals(3, v1.y, 0);
        assertEquals(4, v1.z, 0);
    }

    @Test
    public void sub() throws Exception {
        Vector v1 = new Vector(1, 1, 1);
        v1.sub(1, 2, 3);

        assertEquals(0, v1.x, 0);
        assertEquals(-1, v1.y, 0);
        assertEquals(-2, v1.z, 0);
    }

    @Test
    public void scale() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        v1.scale(1.5);

        assertEquals(1.5, v1.x, 0.001);
        assertEquals(3, v1.y, 0.001);
        assertEquals(4.5, v1.z, 0.001);
    }

    @Test
    public void length() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        double length = v1.length();
        assertEquals(3.7416, length, 0.001);
    }

    @Test
    public void length2() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        double length2 = v1.length2();
        assertEquals(14, length2, 0.001);
    }

    @Test
    public void norm() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        double a = Vector.angle(Vector.X, v1);

        Vector n = v1.norm();
        double b = Vector.angle(Vector.X, n);

        assertEquals(1, n.length(), 0.01);
        assertEquals(a, b, 0.001);
    }

    @Test
    public void cross() throws Exception {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(4, 5, 6);
        Vector v = Vector.cross(v1, v2);

        assertEquals(-3, v.x, 0.001);
        assertEquals(6, v.y, 0.001);
        assertEquals(-3, v.z, 0.001);
    }

    @Test
    public void getAngle() throws Exception {
        double a = Vector.angle(Vector.X, Vector.Y);
        assertEquals(1.5707, a, 0.01);

        a = Vector.angle(Vector.X, new Vector(1, 1, 0));
        assertEquals(0.7854, a, 0.01);
    }

}