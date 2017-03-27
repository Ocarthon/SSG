package de.ocarthon.ssg.curaengine.config;

public class Extruder {
    /**
     * nozzle diameter in mm
     */
    @CuraSetting(key = "machine_nozzle_size")
    public double nozzleSize = 0.35;

    /**
     * nozzle offset on x-Axis in mm
     */
    @CuraSetting(key = "machine_nozzle_offset_x")
    public double nozzleOffsetX = 0;

    /**
     * nozzle offset on y-Axis in mm
     */
    @CuraSetting(key = "machine_nozzle_offset_y")
    public double nozzleOffsetY = 0;

    /**
     * line width. Should be equal to {@link Extruder#nozzleSize}
     */
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

    /**
     * printing temperature
     */
    @CuraSetting(key = "material_print_temperature")
    public double printTemperature = 195;

    /**
     * intermediate temperature to prevent thermal runaway
     */
    public double intermediateTemperature = 200;

    /**
     * material diameter in mm
     */
    @CuraSetting(key = "material_diameter")
    public double materialDiameter = 1.75;

    /**
     * material flow in percent
     */
    @CuraSetting(key = "material_flow")
    public double materialFlow = 100;

    /**
     * layer height in mm. Will be overridden by the one
     * set in the Printer-object
     */
    @CuraSetting(key = "layer_height")
    @CuraSetting(key = "infill_sparse_thickness")
    @CuraSetting(key = "raft_surface_thickness")
    double layerHeight = 0.2;

    /**
     * layer height for first layer in mm. Will be
     * overridden by the one set in the Printer-object
     */@CuraSetting(key = "layer_height_0")
    double layerHeight0 = 0.2;

    /**
     * number of extruder starting from 0
     */
    public final int extruderNr;

    /**
     * whether the extruder has already been primed. This
     * is used internally by the splicer to determine if
     * the material is retracted.
     */
    public boolean isPrimed = false;

    public Extruder(int extruderNr) {
        this.extruderNr = extruderNr;
    }
}
