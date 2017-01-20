package de.ocarthon.ssg;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilamentCalc {
    static final Pattern E_PATTERN = Pattern.compile("E(\\d*\\.*\\d*)");

    public static void main(String[] args) throws IOException {
        File file = new File("Bogen_struc.gcode");
        FileInputStream fis = new FileInputStream(file);

        double currentE = 0;
        double usedFilament = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("G1")) {
                    double newE = readDouble(E_PATTERN, line);
                    usedFilament += newE - currentE;
                    currentE = newE;
                } else if (line.startsWith("G92")) {
                    currentE = readDouble(E_PATTERN, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fis.close();

        System.out.println("Filament used: " + usedFilament);
    }

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
            return -1;
        }
    }
}
