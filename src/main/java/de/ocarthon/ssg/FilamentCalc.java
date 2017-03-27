package de.ocarthon.ssg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilamentCalc {
    static final Pattern E_PATTERN = Pattern.compile("E(-*\\d*\\.*\\d*)");

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
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

        System.out.println("Filament used: " + globalE);
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
            return Double.NaN;
        }
    }
}
