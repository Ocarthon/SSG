package de.ocarthon.ssg.math;

import java.util.ArrayList;
import java.util.List;

public class Object3D {
    public List<Facet> facets;
    public Vector translation = new Vector(0, 0, 0);
    public Vector rotation = new Vector(0, 0, 0);
    public double scalar = 1;
    public int extruderNr = 0;

    public Object3D(int facetCount) {
        facets = new ArrayList<>(facetCount);
    }

    public Object3D() {
        facets = new ArrayList<>(32);
    }

    public void centerObject() {
        Vector avg = new Vector(0, 0, 0);
        double zMin = Double.MAX_VALUE;
        double temp;

        for (Facet f : facets) {
            avg.add(f.p1).add(f.p2).add(f.p3);
            temp = MathUtil.findLowestPoint(f);

            zMin = Math.min(zMin, temp);
        }

        avg.scale(1d/(facets.size() * 3));

        double finalZMin = zMin;
        facets.forEach(f -> {
            f.p1.sub(avg.x, avg.y, finalZMin);
            f.p2.sub(avg.x, avg.y, finalZMin);
            f.p3.sub(avg.x, avg.y, finalZMin);
        });
    }

    public byte[] writeObject() {
        // Facet -> 3 Vectors -> 3 Floats -> 4 Bytes (32 bit)
        byte[] vertices = new byte[facets.size()*3*3*4];
        Matrix rot = Matrix.rotationMatrix(rotation.x, rotation.y, rotation.z).multiply(Matrix.scaleMatrix(scalar));
        for (int i = 0; i < facets.size(); i++) {
            Facet f = rot.transform(facets.get(i));
            writeVector(vertices, i * 3*3*4, f.p1);
            writeVector(vertices, i * 3*3*4 + 12, f.p2);
            writeVector(vertices, i * 3*3*4 + 24, f.p3);
        }

        return vertices;
    }

    private void writeVector(byte[] data, int o, Vector v) {
        writeFloat(data, o, (float) v.x);
        writeFloat(data, o + 4, (float) v.y);
        writeFloat(data, o + 8, (float) v.z);
    }

    private void writeFloat(byte[] data, int o, float f) {
        int fi = Float.floatToIntBits(f);
        data[o + 3] = (byte) ((fi >> 24) & 0xFF);
        data[o + 2] = (byte) ((fi >> 16) & 0xFF);
        data[o + 1] = (byte) ((fi >> 8) & 0xFF);
        data[o] = (byte) (fi & 0xFF);
    }

    @Override
    public String toString() {
        return "Object3D{" +
                "facets=" + facets +
                ", translation=" + translation +
                ", rotation=" + rotation +
                ", scalar=" + scalar +
                '}';
    }
}
