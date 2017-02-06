package de.ocarthon.ssg.gcode;

import de.ocarthon.ssg.curaengine.config.Extruder;
import de.ocarthon.ssg.curaengine.config.Printer;

@FunctionalInterface
interface GCInstruction {
    String convertToGCode(Printer printer, Extruder extruder);
}
