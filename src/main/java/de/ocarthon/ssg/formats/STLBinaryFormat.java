package de.ocarthon.ssg.formats;

import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.math.Vector;

import java.io.IOException;
import java.io.InputStream;

class STLBinaryFormat {

    static Object3D readObject(InputStream in) throws Exception {
        // Skip header
        int b = (int) in.skip(80);
        if (b != 80) {
            throw new MalformedObjectFile("No header");
        }

        // read triangle count
        byte[] data = new byte[4];
        b = in.read(data);
        if (b != 4) {
            throw new MalformedObjectFile("No triangle count");
        }

        int triangleCount = data[0] | data[1] << 8 | data[2] << 16 | data[3] << 24;

        Object3D obj = new Object3D(triangleCount);

        for (int i = 0; i < triangleCount; i++) {
            b = (int) in.skip(12);
            if (b != 12) {
                throw new IOException();
            }

            b = (int) in.skip(2);
            if (b != 2) {
                throw new MalformedObjectFile();
            }

            obj.facets.add(new Facet(readVector(in), readVector(in), readVector(in)));
        }

        return obj;
    }

    private static Vector readVector(InputStream in) throws Exception {
        Vector v = new Vector();
        int total = 0;
        byte[] d = new byte[4];

        total += in.read(d);
        v.x = readFloat(d);

        total += in.read(d);
        v.y = readFloat(d);

        total += in.read(d);
        v.z = readFloat(d);

        if (total != 12) {
            throw new MalformedObjectFile("malformed triangle");
        }

        return v;
    }

    private static float readFloat(byte[] d) {
        return Float.intBitsToFloat((d[0] & 0xFF) << 16 | (d[1] & 0xFF) << 24 | (d[2] & 0xFF) | (d[3] & 0xFF) << 8);
    }
}
