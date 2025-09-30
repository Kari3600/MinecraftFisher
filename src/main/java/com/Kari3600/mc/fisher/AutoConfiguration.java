package com.Kari3600.mc.fisher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface AutoConfiguration {
    String path();
}
