package de.ocarthon.ssg.curaengine.config;

import com.google.protobuf.ByteString;
import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.math.Object3D;

import java.lang.reflect.Field;

public class CuraConfiguration {

    /**
     * Adds the objects in a ObjectList and adds the corresponding translation
     * @param builder builder that the objects will be added to
     * @param objects the objects that will be added
     * @return builder for chaining
     */
    public static Cura.Slice.Builder addObjects(Cura.Slice.Builder builder, Object3D... objects) {
        Cura.ObjectList.Builder objList = Cura.ObjectList.newBuilder();

        for (Object3D obj : objects) {
            objList.addObjects(Cura.Object.newBuilder().setVertices(ByteString.copyFrom(obj.writeObject()))
                    .addSettings(createSetting("center_object", "false"))
                    .addSettings(createSetting("mesh_position_x", String.valueOf(obj.translation.x)))
                    .addSettings(createSetting("mesh_position_y", String.valueOf(obj.translation.y)))
                    .addSettings(createSetting("mesh_position_z", String.valueOf(obj.translation.z))));
        }

        return builder.addObjectLists(objList);
    }

    /**
     * Creates the configuration fot the printer that is given. This includes
     * generating the config for all existing extruder using {@link #createExtruder(Extruder, Printer)},
     * calculating values from the printer configuration and setting
     * constant values
     * @param builder builder that the config will be added to
     * @param printer printer whose config will be added
     * @return builder for chaining
     */
    public static Cura.Slice.Builder setConfiguration(Cura.Slice.Builder builder, Printer printer) {
        // Add all extruder
        for (Extruder ext : printer.extruders.values()) {
            builder.addExtruders(createExtruder(ext, printer));
        }

        // read settings that are inside the [@link Printer} object
        Cura.SettingList.Builder slb = readSettingsFromObject(printer);

        // Calculate settings
        slb.addSettings(createSetting("top_layers", String.valueOf(printer.infillDensity == 100 ? 0 : Math.max(4, Math.ceil(printer.topBottomThickness / printer.layerHeight)))));
        slb.addSettings(createSetting("bottom_layers", String.valueOf(printer.infillDensity == 100 ? 0 : Math.max(4, Math.ceil(printer.topBottomThickness / printer.layerHeight)))));

        // Add constant settings
        slb.addSettings(createSetting("machine_start_gcode", ""));
        slb.addSettings(createSetting("machine_end_gcode", ""));
        slb.addSettings(createSetting("retraction_enable", "true"));
        slb.addSettings(createSetting("material_print_temp_prepend", "false"));
        slb.addSettings(createSetting("material_bed_temp_prepend", "false"));

        slb.addSettings(createSetting("support_extruder_nr", "0"));
        slb.addSettings(createSetting("support_type", "everywhere"));

        return builder.setGlobalSettings(slb);
    }

    /**
     * Creates a extruder configuration
     * @param ext extruder whose configuration will be created
     * @param printer the corresponding printer to obtain setting values
     *                that are required to calculate extruder-specific
     *                values
     * @return builder that contains the extruder configuration
     */
    private static Cura.Extruder.Builder createExtruder(Extruder ext, Printer printer) {
        Cura.Extruder.Builder extConf = Cura.Extruder.newBuilder();
        extConf.setId(ext.extruderNr);

        // Set layer heights
        ext.layerHeight = printer.layerHeight;
        ext.layerHeight0 = printer.layerHeight0;

        // read settings that are inside the Extruder object
        Cura.SettingList.Builder slb = readSettingsFromObject(ext);

        // Calculate settings
        slb.addSettings(createSetting("infill_line_distance", String.valueOf((ext.lineWidth * 100) / printer.infillDensity)));
        slb.addSettings(createSetting("support_line_distance", String.valueOf((ext.lineWidth * 100) / printer.supportInfillDensity)));

        slb.addSettings(createSetting("brim_line_count", String.valueOf(Math.floor(printer.brimWidth / ext.lineWidth))));

        return extConf.setSettings(slb);
    }

    /**
     * Reads all field in the given object that are annotated with the [@link {@link CuraSetting}
     * interface.
     * @param obj objects that the fields will be read from
     * @return builder for {@link Cura.SettingList}
     */
    private static Cura.SettingList.Builder readSettingsFromObject(Object obj) {
        Cura.SettingList.Builder settingList = Cura.SettingList.newBuilder();

        for (Field field : obj.getClass().getDeclaredFields()) {
            // If no CuraSetting-Annotation is present, skip this field
            if (!field.isAnnotationPresent(CuraSetting.class) && !field.isAnnotationPresent(CuraSettings.class)) {
                continue;
            }

            // Set the field accessible to also read private fields
            field.setAccessible(true);

            // Get the annotations of type CuraSetting
            CuraSetting[] settings = field.getAnnotationsByType(CuraSetting.class);

            // If one is present, add it to the list
            if (settings.length == 1) {
                settingList.addSettings(createSetting(obj, field, settings[0]));
            } else {
                // If not they are grouped in the corresponding type
                CuraSettings settings1 = field.getAnnotationsByType(CuraSettings.class)[0];

                for (CuraSetting curaSetting : settings1.value()) {
                    settingList.addSettings(createSetting(obj, field, curaSetting));
                }
            }

            // Just to be safe set the accessible flag of the field to false
            field.setAccessible(false);
        }

        return settingList;
    }

    private static Cura.Setting.Builder createSetting(Object obj, Field field, CuraSetting setting) {
        try {
            return createSetting(setting.key(), field.get(obj).toString());
        } catch (IllegalAccessException e) {
            // Not possible at that point as the field is set
            // to be accessible
            return null;
        }
    }

    private static Cura.Setting.Builder createSetting(String key, String value) {
        return Cura.Setting.newBuilder().setName(key).setValue(ByteString.copyFromUtf8(value));
    }
}
