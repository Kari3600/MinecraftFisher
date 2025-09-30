package com.Kari3600.mc.fisher;

import com.Kari3600.mc.fisher.bukkit.BukkitProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

public class FishContainer {
    private static final FishContainer instance = new FishContainer();
    private static final boolean bukkit;

    static {
        boolean var;
        try {
            Class.forName("org.bukkit.Bukkit");
            var = true;
        } catch (ClassNotFoundException e) {
            var = false;
        }
        bukkit = var;
    }

    public static FishContainer get() {
        return instance;
    }

    private final Map<Class<?>, Object> singletons = new HashMap<>();

    private List<Object> buildArgs(Constructor<?> constructor, Object[] args) {
        List<Object> finalArgs = new ArrayList<>();
        Iterator<Object> iterator = Arrays.asList(args).iterator();
        for (Parameter param : constructor.getParameters()) {
            Class<?> paramType = param.getType();
            if (param.isAnnotationPresent(Hook.class)) {
                finalArgs.add(getFish(paramType));
            } else {
                if (!iterator.hasNext()) return null;
                Object arg = iterator.next();
                if (!paramType.isAssignableFrom(arg.getClass())) return null;
                finalArgs.add(arg);
            }
        }
        if (iterator.hasNext()) return null;
        return finalArgs;
    }

    public <T> T instantiate(Class<T> clazz, Object... args) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            List<Object> finalArgs = buildArgs(constructor, args);
            if (finalArgs != null) {
                try {
                    return clazz.cast(constructor.newInstance(finalArgs.toArray()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz, e);
                }
            }
        }

        throw new RuntimeException("No suitable constructor found for " + clazz);
    }

    public <T> T getFish(Class<T> clazz) {
        if (singletons.containsKey(clazz)) {
            return clazz.cast(singletons.get(clazz));
        }
        T fish = null;
        if (bukkit) {
            fish = BukkitProvider.getRegisteredService(clazz);
        }
        if (fish == null) {
            fish = instantiate(clazz);
        }
        singletons.put(clazz, fish);
        return fish;
    }

    private FishContainer() {

    }

    @SuppressWarnings("unchecked")
    public <T> void register(T fish) {
        register((Class<T>) fish.getClass(),fish);
    }

    public <T> void register(Class<T> clazz, T fish) {
        singletons.put(clazz, fish);
    }
}
