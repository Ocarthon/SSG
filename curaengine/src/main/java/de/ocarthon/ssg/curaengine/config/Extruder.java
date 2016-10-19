package de.ocarthon.ssg.curaengine.config;

public class Extruder {
    public int extruderNr = 0;

    @CuraSetting(key = "machine_nozzle_size")
    public float nozzleSize = 0.35F;

    @CuraSetting(key = "line_width")
    public float lineWidth = 0.35F;

    @CuraSetting(key = "wall_line_width")
    public float wallLineWidth = 0.35F;

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
    float layerHeight = 0.2F;

    @CuraSetting(key = "layer_height_0")
    float layerHeight0 = 0.3F;
}
