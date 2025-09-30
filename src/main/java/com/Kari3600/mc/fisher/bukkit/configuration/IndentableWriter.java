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
