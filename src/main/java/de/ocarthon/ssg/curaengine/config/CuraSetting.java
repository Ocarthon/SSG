package de.ocarthon.ssg.curaengine.config;

import java.lang.annotation.*;

@Repeatable(CuraSettings.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface CuraSetting {
    String key();
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface CuraSettings {
    CuraSetting[] value();
}
