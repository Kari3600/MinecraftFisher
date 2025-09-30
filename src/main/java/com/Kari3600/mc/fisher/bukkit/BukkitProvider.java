package com.Kari3600.mc.fisher.bukkit;

import com.Kari3600.mc.fisher.AnnotationProcessor;
import com.Kari3600.mc.fisher.AutoCommand;
import com.Kari3600.mc.fisher.AutoConfiguration;
import com.Kari3600.mc.fisher.AutoPlugin;
import com.Kari3600.mc.fisher.bukkit.configuration.ConfigurationSection;
import com.Kari3600.mc.fisher.bukkit.configuration.IndentableWriter;
import com.Kari3600.mc.fisher.bukkit.configuration.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class BukkitProvider {
    public static <T> T getRegisteredService(Class<T> clazz) {
        RegisteredServiceProvider<T> rsp = Bukkit.getServicesManager().getRegistration(clazz);
        if (rsp == null) return null;
        return rsp.getProvider();
    }

    public static void generateSources(AnnotationProcessor processor, AutoPlugin plugin, Collection<AutoCommand> commands, Collection<AutoConfiguration> configurables, String mainClass) {
        ConfigurationSection config = new YamlConfiguration();
        config.set("name", plugin.name());
        config.set("version", plugin.version());
        config.set("author", plugin.author());
        config.set("main", mainClass);

        ConfigurationSection commandsSection = config.createSection("commands");
        for (AutoCommand command : commands) {
            ConfigurationSection commandSection = commandsSection.createSection(command.name());
            commandSection.set("description", command.description());
            if (command.aliases().length != 0) commandSection.set("aliases", command.aliases());
            if (!command.permission().isEmpty()) commandSection.set("permission", command.permission());
        }

        try (Writer writer = processor.generateFile("","plugin.yml")) {
            config.write(new IndentableWriter(writer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection config2 = new YamlConfiguration();
        for (AutoConfiguration configurable : configurables) {
            config2.set(configurable.path(), "NULL");
        }

        try (Writer writer = processor.generateFile("","config.yml")) {
            config2.write(new IndentableWriter(writer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
