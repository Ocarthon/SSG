package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

public class GCInstructions {

    public static class G0 implements GCInstruction {
        public double x;
        public double y;
        public double z;
        public double f = -1;

        public G0(double x, double y, double z, double f) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.f = f;
        }

        public G0(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public G0(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String convertToGCode(Printer printer, Extruder extruder) {
            return buildGCode("G0", new char[]{'F', 'X', 'Y', 'Z'}, f, x + printer.width / 2 + extruder.nozzleOffsetX, y + printer.depth / 2 + extruder.nozzleOffsetY, z);
        }
    }

    public static class G1 extends G0 {
        public double e;

        public G1(double x, double y, double z) {
            super(x, y, z);
        }

        public G1(double x, double y, double z, double f) {
            super(x, y, z, f);
        }

        public G1(double x, double y) {
            super(x, y);
        }

        @Override
        public String convertToGCode(Printer printer, Extruder extruder) {
            return buildGCode("G1", new char[]{'F', 'X', 'Y', 'Z', 'E'}, f, x + printer.width / 2 + extruder.nozzleOffsetX, y + printer.depth / 2 + extruder.nozzleOffsetY, z, e);
        }
    }

    public static class G2 extends G1 {
        public double i;
        public double j;

        @Override
        public String convertToGCode(Printer printer, Extruder extruder) {
            return buildGCode("G2", new char[]{'F', 'I', 'J', 'E'}, f, i, j, e);
        }

        public G2(double i, double j) {
            super(0, 0);

            this.i = i;
            this.j = j;
        }
    }

    private static String buildGCode(String command, char[] names, double... values) {
        StringBuilder sb = new StringBuilder();
        sb.append(command);

        for (int i = 0; i < names.length; i++) {
            if (values[i] != 0 || names[i] != 'Z') {
                sb.append(String.format(names[i] == 'F' ? " %s%.0f" : names[i] == 'E' ? " %s%.5f" : " %s%.3f", names[i], values[i]));
            }
        }
        return sb.toString();
    }
}
