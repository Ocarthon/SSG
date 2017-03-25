package de.ocarthon.ssg.curaengine.config;

public class Extruder {
    public int extruderNr = 0;
    
    public boolean isPrimed = false;

    @CuraSetting(key = "machine_nozzle_size")
    public double nozzleSize = 0.35;

    @CuraSetting(key = "line_width")
    @CuraSetting(key = "wall_line_width")
    @CuraSetting(key = "wall_line_width_0")
    @CuraSetting(key = "wall_line_width_x")
    @CuraSetting(key = "skin_line_wid th")
    @CuraSetting(key = "infill_line_width")
    @CuraSetting(key = "skirt_brim_line_width")
    @CuraSetting(key = "support_line_width")
    @CuraSetting(key = "support_interface_line_width")
    public double lineWidth = 0.35;

    @CuraSetting(key = "material_print_temperature")
    public double printTemperature = 195;

    // intermediate temperature to prevent thermal runaway
    public double intermediateTemperature = 205;

    public double standbyTemperature = 195;

    @CuraSetting(key = "material_diameter")
    public double materialDiameter = 1.75;

    @CuraSetting(key = "material_flow")
    public double materialFlow = 100;

    @CuraSetting(key = "machine_nozzle_offset_x")
    public double nozzleOffsetX = 0;

    @CuraSetting(key = "machine_nozzle_offset_y")
    public double nozzleOffsetY = 0;

    @CuraSetting(key = "layer_height")
    @CuraSetting(key = "infill_sparse_thickness")
    @CuraSetting(key = "raft_surface_thickness")
    public double layerHeight = 0.2;

    @CuraSetting(key = "layer_height_0")
    double layerHeight0 = 0.2;
}
