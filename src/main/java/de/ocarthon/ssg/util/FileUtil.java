package de.ocarthon.ssg.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtil {

    public static void saveResourceToFile(InputStream in, File file) throws IOException {
        if (file != null && !file.exists()) {
            Files.copy(in, file.toPath());
        }
    }

    public static void write(OutputStream out, String format, Object... args) throws IOException {
        out.write(String.format(format, args).getBytes(StandardCharsets.UTF_8));
    }
}
