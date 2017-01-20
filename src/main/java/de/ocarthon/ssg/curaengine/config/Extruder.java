package de.ocarthon.ssg.curaengine.config;

public class Extruder {
    public int extruderNr = 0;

    @CuraSetting(key = "machine_nozzle_size")
    public float nozzleSize = 0.35F;

    @CuraSetting(key = "line_width")
    @CuraSetting(key = "wall_line_width")
    @CuraSetting(key = "wall_line_width_0")
    @CuraSetting(key = "wall_line_width_x")
    @CuraSetting(key = "skin_line_wid th")
    @CuraSetting(key = "infill_line_width")
    @CuraSetting(key = "skirt_brim_line_width")
    @CuraSetting(key = "support_line_width")
    @CuraSetting(key = "support_interface_line_width")
    public float lineWidth = 0.35F;

    @CuraSetting(key = "material_print_temperature")
    public float printTemperature = 200F;

    @CuraSetting(key = "material_diameter")
    public float materialDiameter = 1.75F;

    @CuraSetting(key = "material_flow")
    public float materialFlow = 100F;

    @CuraSetting(key = "machine_nozzle_offset_x")
    public float nozzleOffsetX = 0;

    @CuraSetting(key = "machine_nozzle_offset_y")
    public float nozzleOffsetY = 0;

    @CuraSetting(key = "layer_height")
    @CuraSetting(key = "infill_sparse_thickness")
    @CuraSetting(key = "raft_surface_thickness")
    public float layerHeight = 0.2F;

    @CuraSetting(key = "layer_height_0")
    float layerHeight0 = 0.2F;
}
