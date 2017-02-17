package de.ocarthon.ssg.formats;

import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.math.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class STLASCIIFormat {

    static Object3D readObject(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        reader.readLine(); // Header

        Object3D obj = new Object3D();

        while (true) {
            String normal = readLine(reader);
            if (normal == null || normal.startsWith("endsolid")) {
                break;
            }

            Vector n = readVertex(normal);

            readLine(reader); // outer loop
            Vector p1 = readVertex(readLine(reader));
            Vector p2 = readVertex(readLine(reader));
            Vector p3 = readVertex(readLine(reader));
            readLine(reader); // endloop

            readLine(reader); // endfacet

            obj.facets.add(new Facet(p1, p2, p3, n));
        }

        reader.close();

        return obj;
    }

    private static String readLine(BufferedReader reader) throws IOException {
        String line = null;
        while (line == null || line.isEmpty()) {
            line = reader.readLine();
        }

        return line;
    }

    private static Vector readVertex(String s) {
        Vector v = new Vector(0, 0, 0);
        int i = s.lastIndexOf(32);
        v.z = Double.parseDouble(s.substring(i));

        int i2 = s.lastIndexOf(32, i - 1);
        v.y = Double.parseDouble(s.substring(i2, i));

        i = s.lastIndexOf(32, i2 - 1);
        v.x = Double.parseDouble(s.substring(i, i2));

        return v;
    }
}
