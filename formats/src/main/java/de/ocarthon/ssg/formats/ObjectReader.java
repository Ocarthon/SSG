package de.ocarthon.ssg.formats;

import de.ocarthon.ssg.math.Object3D;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ObjectReader {

    public static Object3D readObject(File file) throws Exception {
        String[] p = file.getName().split(".");
        String ending = p[p.length-1];

        switch (ending) {
            case "obj":
                return WavefrontFormat.readObject(new FileInputStream(file));
            case "stl":
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[5];
                int l = fis.read(data);
                fis.close();
                if (l != data.length) {
                    throw new IOException("no valid file!");
                } else if (new String(data, StandardCharsets.US_ASCII).equals("solid")) {
                    return STLASCIIFormat.readObject(new FileInputStream(file));
                } else {
                    return STLBinaryFormat.readObject(new FileInputStream(file));
                }
            default:
                throw new IOException("unknown file format");
        }
    }
}
