package com.Kari3600.mc.fisher.bukkit.configuration;

import java.io.IOException;

public class ValueSection extends AbstractSection {
    private final String value;

    @Override
    public void write(IndentableWriter writer) throws IOException {
        writer.write(value);
    }

    public ValueSection(String value) {
        this.value = value;
    }
}
