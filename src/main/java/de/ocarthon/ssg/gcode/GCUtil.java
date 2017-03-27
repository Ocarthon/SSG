package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.ocarthon.ssg.util.FileUtil.write;

public class GCUtil {
    public static final Pattern X_PATTERN = Pattern.compile("X(\\d*\\.*\\d*)");
    public static final Pattern Y_PATTERN = Pattern.compile("Y(\\d*\\.*\\d*)");
    public static final Pattern Z_PATTERN = Pattern.compile("Z(\\d*\\.*\\d*)");
    public static final Pattern E_PATTERN = Pattern.compile("E(-*\\d*\\.*\\d*)");

    public static String applyOffset(String line, Extruder extruder) {
        if (!line.startsWith("G1") && !line.startsWith("G0") && !line.startsWith("G2")) {
            return line;
        }

        if (extruder.nozzleOffsetX == 0 && extruder.nozzleOffsetY == 0) {
            return line;
        }

        double x = readDouble(X_PATTERN, line) + extruder.nozzleOffsetX;
        double y = readDouble(Y_PATTERN, line) + extruder.nozzleOffsetY;

        line = X_PATTERN.matcher(line).replaceFirst(String.format("X%.5f", x));
        line = Y_PATTERN.matcher(line).replaceFirst(String.format("Y%.5f", y));

        return line;
    }

    public static double readDouble(Pattern pattern, String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            String s = m.group().trim().substring(1);
            if (s.length() == 0) {
                return -1;
            } else {
                return Double.parseDouble(s);
            }
        } else {
            return -1;
        }
    }

    public static void writeComment(OutputStream out, String comment) throws IOException {
        write(out, "; %s%n", comment);
        write(out, "M117 %s%n", comment);
    }
}
