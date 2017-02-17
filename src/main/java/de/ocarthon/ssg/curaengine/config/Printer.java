package de.ocarthon.ssg.curaengine.config;

import de.ocarthon.ssg.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class Printer {
    //
    // Extruder settings
    //
    List<Extruder> extruders = new ArrayList<>();

    //
    // Machine settings
    //

    @CuraSetting(key = "machine_width")
    public double width = 240;

    @CuraSetting(key = "machine_depth")
    public double depth = 215;

    @CuraSetting(key = "machine_height")
    public double height = 190;

    @CuraSetting(key = "machine_extruder_count")
    public int extruderCount = 0;

    @CuraSetting(key = "machine_gcode_flavor")
    public String gcodeFlavour = "RepRap (Marlin/Sprinter)";

    @CuraSetting(key = "machine_start_gcode")
    String machineStartGCode = "";

    public String startGCode = "G28 ;Home\nG1 Z15.0 F6000 ;G0 the platform down 15mm\n;Prime the extruder\nG92 E0\nG1 F200 E3\nG92 E0";

    @CuraSetting(key = "machine_end_gcode")
    String machineStopGCode = "";

    public String endGCode = "M104 S0\nM140 S0\n;Retract the filament\nG92 E1\nG1 E-1 F300\nG28 X0 Y0\nM84";

    @CuraSetting(key = "cool_fan_full_layer")
    public int fanAtLayer = 1;

    public boolean useG2 = true;

    //
    // Print settings
    //

    public Vector origin = new Vector(0, 20, 0);

    @CuraSetting(key = "layer_height")
    public double layerHeight = 0.2;

    @CuraSetting(key = "layer_height_0")
    public double layerHeight0 = 0.2;

    @CuraSetting(key = "speed_print")
    public double printSpeed = 45;

    @CuraSetting(key = "speed_travel")
    public double travelSpeed = 120;

    @CuraSetting(key = "top_bottom_thickness")
    public double topBottomThickness = 0.8;

    @CuraSetting(key = "adhesion_type")
    public String adhesionType = "skirt";

    @CuraSetting(key = "skirt_line_count")
    public int skirtLineCount = 4;

    @CuraSetting(key = "infill_sparse_density")
    public int infillDensity = 5;

    @CuraSetting(key = "retraction_amount")
    public double retractionAmount = 6;

    public double retractionSpeed = 70 * 60;

    //public Vector nozzleSwitchPosition = new Vector(40, 40, 0);
    public Vector nozzleSwitchPosition = new Vector(120, 90, 0);

    public double nozzleSwitchRetractionAmount = 12;

    public double nozzleSwitchRetractionSpeed = 80 * 60;

    //
    // Support
    //
    @CuraSetting(key = "support_enable")
    public boolean useSupport = false;

    public boolean useDualPrint = true;

    @CuraSetting(key = "support_type")
    public String supportType = "everywhere";

    @CuraSetting(key = "support_pattern")
    public String supportPattern = "zigzag";

    @CuraSetting(key = "support_angle")
    public double supportAngle = 45;

    @CuraSetting(key = "support_extruder_nr")
    public int supportExtruderNr = 0;

    @CuraSetting(key = "speed_support")
    public double supportSpeed = 60;

    @CuraSetting(key = "support_infill_rate")
    public int supportInfillDensity = 15;

    //
    // Priming tower
    //
    public boolean usePrimeTower = true;

    public double primeTowerX = 20;

    public double primeTowerY = 40;

    public double primeTowerSize = 7;


    public void addExtruder(Extruder ext) {
        extruders.add(ext);
        extruderCount++;
    }

    public Extruder getExtruder(int i) {
        return extruders.get(i);
    }

    public static Printer k8400() {
        Printer printer = new Printer();

        Extruder ext1 = new Extruder();
        
        Extruder ext2 = new Extruder();
        ext2.extruderNr = 1;
        //ext2.nozzleOffsetX = -23.7F;
        ext2.nozzleOffsetX = -24.7;
        //ext2.isPrimed = false;
        
        printer.addExtruder(ext1);
        printer.addExtruder(ext2);

        return printer;
    }
}

