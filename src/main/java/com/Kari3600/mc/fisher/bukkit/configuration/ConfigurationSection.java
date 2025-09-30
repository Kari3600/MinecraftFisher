package com.Kari3600.mc.fisher.bukkit.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationSection extends AbstractSection {
    protected Map<String, AbstractSection> map = new HashMap<>();

    public void set(String key, String value) {
        set(key, new ValueSection(value));
    }

    public void set(String key, String[] values) {
        set(key, new ShortListSection(values));
    }

    public void set(String _key, AbstractSection value) {
        int index = _key.indexOf('.');
        System.out.println("Checking "+_key);
        System.out.println("Dot index: "+index);
        if (index == -1) {
            map.put(_key, value);
        } else {
            String key = _key.substring(0, index);
            String subkey = _key.substring(index + 1);
            AbstractSection v = map.get(key);
            ConfigurationSection section;
            if (v == null) {
                section = createSection(key);
            } else {
                if (!(v instanceof ConfigurationSection)) throw new IllegalStateException("Error while parsing key: " + key);
                section = (ConfigurationSection) v;
            }
            section.set(subkey, value);
        }
    }

    public ConfigurationSection createSection(String key) {
        ConfigurationSection section = new ConfigurationSection();
        map.put(key, section);
        return section;
    }


    @Override
    public void write(IndentableWriter writer) throws IOException {
        writer.begin();
        for (Map.Entry<String, AbstractSection> entry : map.entrySet()) {
            writer.newLine();
            writer.write(entry.getKey()+": ");
            entry.getValue().write(writer);
        }
        writer.end();
    }
}
