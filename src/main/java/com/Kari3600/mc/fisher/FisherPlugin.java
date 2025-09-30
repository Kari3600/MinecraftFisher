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
