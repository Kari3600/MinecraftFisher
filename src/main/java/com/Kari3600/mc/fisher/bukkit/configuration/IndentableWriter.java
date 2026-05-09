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

package com.Kari3600.mc.fisher.bukkit.configuration;

import java.io.IOException;
import java.io.Writer;

public class IndentableWriter {
    private final Writer writer;
    private int indent = 0;

    public void write(CharSequence text) throws IOException {
        writer.append(text);
    }

    public void newLine() throws IOException {
        writer.append("\n");
        for (int i = 0; i < indent; i++) {
            writer.append("  ");
        }
    }

    public void begin() {
        indent++;
    }

    public void end() {
        indent--;
    }

    public IndentableWriter(Writer writer) {
        this.writer = writer;
    }
}
