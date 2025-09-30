package com.Kari3600.mc.fisher.bukkit.configuration;

import java.io.IOException;
import java.util.Map;

public class YamlConfiguration extends ConfigurationSection {
    @Override
    public void write(IndentableWriter writer) throws IOException {
        boolean first = true;
        for (Map.Entry<String, AbstractSection> entry : map.entrySet()) {
            if (!first) {
                writer.newLine();
            } else {
                first = false;
            }
            writer.write(entry.getKey()+": ");
            entry.getValue().write(writer);
        }
    }
}
