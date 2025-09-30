package com.Kari3600.mc.fisher.bukkit.configuration;

import java.io.IOException;

public abstract class AbstractSection {
    public abstract void write(IndentableWriter writer) throws IOException;
}
