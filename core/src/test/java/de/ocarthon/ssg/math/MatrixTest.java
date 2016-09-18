package de.ocarthon.ssg.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class MatrixTest {

    @Test
    public void multiply() throws Exception {
        Matrix m1 = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix m = m1.multiply(m1);

        assertEquals(30, m.v[0], 0.0001);
        assertEquals(36, m.v[1], 0.0001);
        assertEquals(42, m.v[2], 0.0001);
        assertEquals(66, m.v[3], 0.0001);
        assertEquals(81, m.v[4], 0.0001);
        assertEquals(96, m.v[5], 0.0001);
        assertEquals(102, m.v[6], 0.0001);
        assertEquals(126, m.v[7], 0.0001);
        assertEquals(150, m.v[8], 0.0001);
    }

    @Test
    public void transform() throws Exception {
        Matrix m = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Vector v = m.transform(new Vector(1, 2, 3));

        assertEquals(14, v.x, 0.0001);
        assertEquals(32, v.y, 0.0001);
        assertEquals(50, v.z, 0.0001);
    }

    @Test
    public void rotationMatrix() throws Exception {
        double a = Math.toRadians(45);
        Matrix m = Matrix.rotationMatrix(a, a, a);

        assertEquals(0.5     , m.v[0], 0.0001);
        assertEquals(-0.5    , m.v[1], 0.0001);
        assertEquals(0.707107, m.v[2], 0.0001);
        assertEquals(0.853553, m.v[3], 0.0001);
        assertEquals(0.146446, m.v[4], 0.0001);
        assertEquals(-0.5    , m.v[5], 0.0001);
        assertEquals(0.146447, m.v[6], 0.0001);
        assertEquals(0.853553, m.v[7], 0.0001);
        assertEquals(0.5     , m.v[8], 0.0001);
    }

}