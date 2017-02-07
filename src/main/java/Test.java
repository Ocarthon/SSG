import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.gcode.PrimeTower;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class Test {

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ENGLISH);
        Printer printer = new Printer();
        Extruder ext = new Extruder();
        printer.addExtruder(ext);

        PrimeTower pt = new PrimeTower(printer);

        FileOutputStream fos = new FileOutputStream("prime.gcode");
        double e = pt.printLayer(fos, printer, ext);
        pt.printLayer(fos, printer, ext);

        fos.flush();
        fos.close();

    }
}
