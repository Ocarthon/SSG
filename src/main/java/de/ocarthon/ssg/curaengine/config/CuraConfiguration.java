package de.ocarthon.ssg.curaengine.config;

import com.google.protobuf.ByteString;
import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.math.Object3D;

import java.lang.reflect.Field;

public class CuraConfiguration {

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

    public static Cura.Slice.Builder setConfiguration(Cura.Slice.Builder builder, Printer printer) {
        for (Extruder ext : printer.extruders) {
            builder.addExtruders(createExtruder(ext, printer));
        }

        Cura.SettingList.Builder slb = readSettingsFromObject(printer);
        slb.addSettings(createSetting("top_layers", String.valueOf(printer.infillDensity == 100 ? 0 : Math.max(4, Math.ceil(printer.topBottomThickness / printer.layerHeight)))));
        slb.addSettings(createSetting("bottom_layers", String.valueOf(printer.infillDensity == 100 ? 0 : Math.max(4, Math.ceil(printer.topBottomThickness / printer.layerHeight)))));
        slb.addSettings(createSetting("retraction_enable", "true"));
        slb.addSettings(createSetting("material_print_temp_prepend", "false"));
        slb.addSettings(createSetting("material_bed_temp_prepend", "false"));

        builder.setGlobalSettings(slb);

        return builder;
    }

    private static Cura.Extruder createExtruder(Extruder ext, Printer printer) {
        Cura.Extruder.Builder extConf = Cura.Extruder.newBuilder();
        extConf.setId(ext.extruderNr);

        Cura.SettingList.Builder slb = readSettingsFromObject(ext);
        slb.addSettings(createSetting("infill_line_distance", String.valueOf((ext.lineWidth * 100) / printer.infillDensity)));
        slb.addSettings(createSetting("support_line_distance", String.valueOf((ext.lineWidth * 100) / printer.supportInfillDensity)));

        extConf.setSettings(slb);
        return extConf.build();
    }

    private static Cura.SettingList.Builder readSettingsFromObject(Object obj) {
        Cura.SettingList.Builder settingList = Cura.SettingList.newBuilder();

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(CuraSetting.class) && !field.isAnnotationPresent(CuraSettings.class)) {
                continue;
            }

            field.setAccessible(true);

            CuraSetting[] settings = field.getAnnotationsByType(CuraSetting.class);
            if (settings.length == 1) {
                settingList.addSettings(createSetting(obj, field, settings[0]));
            } else {
                CuraSettings settings1 = field.getAnnotationsByType(CuraSettings.class)[0];

                for (CuraSetting curaSetting : settings1.value()) {
                    settingList.addSettings(createSetting(obj, field, curaSetting));
                }
            }

        }

        return settingList;
    }

    private static Cura.Setting createSetting(Object obj, Field field, CuraSetting setting) {
        try {
            return createSetting(setting.key(), field.get(obj).toString());
        } catch (IllegalAccessException e) {
            // Not possible at that point
        }

        return null;
    }

    private static Cura.Setting createSetting(String key, String value) {
        return Cura.Setting.newBuilder().setName(key).setValue(ByteString.copyFromUtf8(value)).build();
    }
}
