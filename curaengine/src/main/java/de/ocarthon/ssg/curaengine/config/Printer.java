package de.ocarthon.ssg.curaengine.config;

import java.util.ArrayList;
import java.util.List;

public class Printer {
    List<Extruder> extruders = new ArrayList<>();

    @CuraSetting(key="machine_start_gcode")
    public String startGcode = "G28 ;Home\nG1 Z15.0 F6000 ;Move the platform down 15mm\n;Prime the extruder\nG92 E0\nG1 F200 E3\nG92 E0";

    @CuraSetting(key="machine_end_gcode")
    public String endGcode = "M104 S0\nM140 S0\n;Retract the filament\nG92 E1\nG1 E-1 F300\nG28 X0 Y0\nM84";

    @CuraSetting(key="machine_width")
    public float width = 240F;

    @CuraSetting(key="machine_depth")
    public float depth = 215F;

    @CuraSetting(key="machine_height")
    public float height = 190F;

    @CuraSetting(key="machine_extruder_count")
    public int extruderCount = 0;

    @CuraSetting(key = "machine_gcode_flavor")
    public String gcodeFlavour = "RepRap (Marlin/Sprinter)";

    @CuraSetting(key = "center_object")
    public boolean centerObject = true;

    public void addExtruder(Extruder ext) {
        extruders.add(ext);
        extruderCount++;
    }
}

