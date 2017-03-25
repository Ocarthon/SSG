package de.ocarthon.ssg;

import de.ocarthon.ssg.generator.Generator;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompareUsage {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("No object!");
            return;
        }

        String fileName = args[0];

        Generator.main(new String[]{fileName, "cmpusg_struc.gcode"});
        Thread.sleep(3000);
        Slice.main(new String[]{fileName, "cmpusg_norm.gcode", "false"});
        Thread.sleep(3000);
        Slice.main(new String[]{fileName, "cmpusg_sup.gcode", "true"});
        Thread.sleep(3000);
        double usgStruc = calcFilament(new File("cmpusg_struc.gcode"));
        double usgNorm = calcFilament(new File("cmpusg_norm.gcode"));
        double usgSup = calcFilament(new File("cmpusg_sup.gcode"));

        System.out.println(usgNorm + "  " + usgSup + "  " + usgStruc + " (" + Math.abs(usgStruc - usgSup) + ")");
    }

    private static double calcFilament(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        double currentE = 0;
        double globalE = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("G1")) {
                    double newE = readDouble(E_PATTERN, line);
                    if (!((Double) (Double.NaN)).equals(newE)) {
                        globalE += newE - currentE;
                        currentE = newE;
                    }
                } else if (line.startsWith("G92")) {
                    currentE = readDouble(E_PATTERN, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fis.close();

        return globalE;
    }

    static final Pattern E_PATTERN = Pattern.compile("E(-*\\d*\\.*\\d*)");

    private static double readDouble(Pattern pattern, String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            String s = m.group().trim().substring(1);
            if (s.length() == 0) {
                return -1;
            } else {
                return Double.parseDouble(s);
            }
        } else {
            return Double.NaN;
        }
    }
}
