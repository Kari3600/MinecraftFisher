package com.Kari3600.mc.fisher.bukkit.configuration;

import java.io.IOException;

public class ShortListSection extends AbstractSection {
    private final String[] values;

    @Override
    public void write(IndentableWriter writer) throws IOException {
        writer.write("[");
        writer.write(String.join(", ", values));
        writer.write("]");
    }

    public ShortListSection(String[] values) {
        this.values = values;
    }
}
