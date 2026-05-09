/*
 * Copyright (c) 2026. Kari3600.
 * This file is part of MinecraftFisher.
 *
 * MinecraftFisher is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MinecraftFisher is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MinecraftFisher. If not, see <https://www.gnu.org/licenses/>.
 */

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
        if (!plugin.apiVersion().isEmpty()) {
            config.set("api-version", plugin.apiVersion());
        }
        if (plugin.depend().length > 0) {
            config.set("depend", plugin.depend());
        }
        if (plugin.softDepend().length > 0) {
            config.set("softdepend", plugin.softDepend());
        }

        if (!commands.isEmpty()) {
            ConfigurationSection commandsSection = config.createSection("commands");
            for (AutoCommand command : commands) {
                ConfigurationSection commandSection = commandsSection.createSection(command.name());
                commandSection.set("description", command.description());
                if (command.aliases().length != 0) commandSection.set("aliases", command.aliases());
                if (!command.permission().isEmpty()) commandSection.set("permission", command.permission());
            }
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
