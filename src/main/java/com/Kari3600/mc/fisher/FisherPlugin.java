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

package com.Kari3600.mc.fisher;

public abstract class FisherPlugin<IPlugin> {
    private final IPlugin wrappedPlugin;
    public abstract void onLoad();
    public abstract void onEnable();
    public abstract void onDisable();
    public IPlugin getWrappedPlugin() {
        return wrappedPlugin;
    }

    public FisherPlugin(IPlugin wrappedPlugin) {
        this.wrappedPlugin = wrappedPlugin;
    }
}
