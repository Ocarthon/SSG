package de.ocarthon.ssg.formats;

import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.math.Vector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class WavefrontFormat {

    static Object3D readObject(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        Object3D obj = new Object3D();

        List<Vector> vectors = new ArrayList<>(16);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() <= 1) continue;
            char cmd = line.charAt(0);

            switch (cmd) {
                case 'v':
                    String[] p = line.trim().split(" ");
                    if (p.length < 4) {
                        throw new MalformedObjectFile();
                    }

                    vectors.add(new Vector(Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3])));
                    break;

                case 'f':
                    p = line.trim().split(" ");
                    if (p.length < 4) {
                        throw new MalformedObjectFile();
                    }

                    obj.facets.add(new Facet(
                            vectors.get(Integer.parseInt(p[1].split("/")[0]) - 1).copy(),
                            vectors.get(Integer.parseInt(p[2].split("/")[0]) - 1).copy(),
                            vectors.get(Integer.parseInt(p[3].split("/")[0]) - 1).copy()
                    ));
            }
        }

        return obj;
    }
}
