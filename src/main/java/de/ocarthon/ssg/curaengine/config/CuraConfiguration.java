package de.ocarthon.ssg.curaengine.config;

import com.google.protobuf.ByteString;
import de.ocarthon.ssg.curaengine.Cura;
import de.ocarthon.ssg.math.Facet;
import de.ocarthon.ssg.math.Matrix;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.math.Vector;

import java.lang.reflect.Field;

public class CuraConfiguration {

    public static Cura.Slice.Builder addObjects(Cura.Slice.Builder builder, Printer printer, Object3D... objects) {
        Cura.ObjectList.Builder objList = Cura.ObjectList.newBuilder();

        for (Object3D obj : objects) {
            objList.addObjects(Cura.Object.newBuilder().setVertices(ByteString.copyFrom(writeObject(obj)))
                    .addSettings(createSetting("mesh_position_x", String.valueOf(obj.translation.x)))
                    .addSettings(createSetting("mesh_position_y", String.valueOf(obj.translation.y)))
                    .addSettings(createSetting("mesh_position_z", String.valueOf(obj.translation.z))));
        }

        return builder.addObjectLists(objList);
    }

    public static Cura.Slice.Builder setConfiguration(Cura.Slice.Builder builder, Printer printer) {
        for (Extruder ext : printer.extruders) {
            builder.addExtruders(createExtruder(ext));
        }

        builder.setGlobalSettings(readSettingsFromObject(printer));
        return builder;
    }

    private static Cura.Extruder createExtruder(Extruder ext) {
        Cura.Extruder.Builder extConf = Cura.Extruder.newBuilder();
        extConf.setId(ext.extruderNr);
        extConf.setSettings(readSettingsFromObject(ext));
        return extConf.build();
    }

    public static Cura.SettingList readSettingsFromObject(Object obj) {
        Cura.SettingList.Builder settingList = Cura.SettingList.newBuilder();

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(CuraSetting.class) && !field.isAnnotationPresent(CuraSettings.class)) {
                continue;
            }

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

        return settingList.build();
    }

    private static Cura.Setting createSetting(Object obj, Field field, CuraSetting setting) {
        try {
            System.out.println("SETTING " + setting.key());
            return createSetting(setting.key(), field.get(obj).toString());
        } catch (IllegalAccessException e) {
            // Not possible at that point
        }

        return null;
    }

    private static Cura.Setting createSetting(String key, String value) {
        return Cura.Setting.newBuilder().setName(key).setValue(ByteString.copyFromUtf8(value)).build();
    }



    public static byte[] writeObject(Object3D obj) {
        // Facet -> 3 Vectors -> 3 Floats -> 4 Bytes (32 bit)
        byte[] vertices = new byte[obj.facets.size()*3*3*4];
        Matrix rot = Matrix.rotationMatrix(obj.rotation.x, obj.rotation.y, obj.rotation.z).multiply(Matrix.scaleMatrix(obj.scalar));
        for (int i = 0; i < obj.facets.size(); i++) {
            Facet f = rot.transform(obj.facets.get(i));
            writeVector(vertices, i * 3*3*4, f.p1);
            writeVector(vertices, i * 3*3*4 + 12, f.p2);
            writeVector(vertices, i * 3*3*4 + 24, f.p3);
        }

        return vertices;
    }

    private static void writeVector(byte[] data, int o, Vector v) {
        writeFloat(data, o, (float) v.x);
        writeFloat(data, o + 4, (float) v.y);
        writeFloat(data, o + 8, (float) v.z);
    }

    private static void writeFloat(byte[] data, int o, float f) {
        int fi = Float.floatToIntBits(f);
        data[o + 3] = (byte) ((fi >> 24) & 0xFF);
        data[o + 2] = (byte) ((fi >> 16) & 0xFF);
        data[o + 1] = (byte) ((fi >> 8) & 0xFF);
        data[o] = (byte) (fi & 0xFF);
    }
}
