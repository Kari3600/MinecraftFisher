package com.Kari3600.mc.fisher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface AutoCommand {
    String name();
    String description() default "Default description";
    String permission() default "";
    String[] aliases() default {};
}
