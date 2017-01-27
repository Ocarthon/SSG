package de.ocarthon.ssg.curaengine.config;

import java.util.ArrayList;
import java.util.List;

public class Printer {
    List<Extruder> extruders = new ArrayList<>();

    @CuraSetting(key="machine_start_gcode")
    String machineStartGCode = "";

    public String startGcode = "G28 ;Home\nG1 Z15.0 F6000 ;Move the platform down 15mm\n;Prime the extruder\nG92 E0\nG1 F200 E3\nG92 E0";

    @CuraSetting(key="machine_end_gcode")
    String machineStopGCode = "";

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
    public boolean centerObject = false;

    @CuraSetting(key = "support_enable")
    public boolean useSupport = false;

    @CuraSetting(key = "support_type")
    public String supportType = "everywhere";

    @CuraSetting(key = "support_pattern")
    public String supportPattern = "zigzag";

    @CuraSetting(key = "support_angle")
    public float supportAngle = 45;

    @CuraSetting(key = "support_extruder_nr")
    public int supportExtruderNr = 0;

    @CuraSetting(key = "speed_support")
    public float supportSpeed = 60;

    @CuraSetting(key = "retraction_enable")
    public boolean retraction = true;

    @CuraSetting(key = "retraction_amount")
    public float retractionAmount = 6;

    @CuraSetting(key = "layer_height")
    public float layerHeight = 0.2F;

    @CuraSetting(key = "layer_height_0")
    public float layerHeight0 = 0.2F;

    @CuraSetting(key = "speed_print")
    public double printSpeed = 60;

    @CuraSetting(key = "speed_travel")
    public double travelSpeed = 120;

    @CuraSetting(key = "cool_fan_full_layer")
    public int fanAtLayer = 1;

    public void addExtruder(Extruder ext) {
        extruders.add(ext);
        extruderCount++;
    }

    public Extruder getExtruder(int i) {
        return extruders.get(i);
    }

    public boolean retractionEnabled() {
        return retraction && retractionAmount != 0;
    }
}

