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
        byte[] data = new byte[12 * 4 + 2];
        b = in.read(data, 0, 4);
        if (b != 4) {
            throw new MalformedObjectFile("No triangle count");
        }

        int triangleCount = data[0] | data[1] << 8 | data[2] << 16 | data[3] << 24;

        Object3D obj = new Object3D(triangleCount);


        for (int i = 0; i < triangleCount; i++) {
            if (in.read(data) != data.length) {
                throw new MalformedObjectFile();
            }

            obj.facets.add(new Facet(readVector(data, 14), readVector(data, 26), readVector(data, 38)));
        }

        return obj;
    }

    private static Vector readVector(byte[] data, int o) {
        return new Vector(readFloat(data, o), readFloat(data, o + 4), readFloat(data, o + 8));
    }

    private static float readFloat(byte[] d, int o) {
        return Float.intBitsToFloat((d[o] & 0xFF) << 16 | (d[o + 1] & 0xFF) << 24 | (d[o + 2] & 0xFF) | (d[o + 3] & 0xFF) << 8);
    }
}
