package com.Kari3600.mc.fisher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface AutoPlugin {
    String name();
    String version();
    String author();
    String apiVersion() default "";
}
