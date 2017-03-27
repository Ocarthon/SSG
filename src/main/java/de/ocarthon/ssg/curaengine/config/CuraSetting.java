package de.ocarthon.ssg.curaengine.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
