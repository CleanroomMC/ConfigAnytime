package com.cleanroommc.configanytime;

import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.*;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Mod(modid = "configanytime", name = "ConfigAnytime", version = "2.0", acceptableRemoteVersions = "[1.0,10)")
public class ConfigAnytime {

    // Lookup#findStatic is used as getDeclaredMethod forcefully loads in classes related to any methods in the class body
    private static final MethodHandle CONFIGMANAGER$SYNC;

    static {
        try {
            Class.forName("net.minecraftforge.common.config.ConfigManager", true, Launch.classLoader); // Init first
            // Max privilege
            Field lookup$impl_lookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            lookup$impl_lookup.setAccessible(true);
            Lookup lookup = ((Lookup) lookup$impl_lookup.get(null)).in(ConfigManager.class);
            CONFIGMANAGER$SYNC = lookup.findStatic(ConfigManager.class, "sync", MethodType.methodType(void.class, Configuration.class, Class.class, String.class, String.class, boolean.class, Object.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register configuration class that is annotated with {@link Config} here for it to be processed immediately with saving and loading supported.
     * Preferably call this method in a static init block at the very end of your configuration class.
     * @param configClass configuration class that is annotated with {@link Config}
     */
    public static void register(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(Config.class)) {
            return;
        }
        try {
            Method classLoader$findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            classLoader$findLoadedClass.setAccessible(true);
            if (classLoader$findLoadedClass.invoke(Launch.classLoader, "net.minecraftforge.fml.common.Loader") != null) {
                if (!Loader.instance().hasReachedState(LoaderState.PREINITIALIZATION)) {
                    // Early
                    $register(configClass);
                }
            } else {
                // Early
                $register(configClass);
            }
            // Late, nothing should be done
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void $register(Class<?> configClass) throws Throwable {
        Field configManager$mod_config_classes = ConfigManager.class.getDeclaredField("MOD_CONFIG_CLASSES");
        Field configManager$configs = ConfigManager.class.getDeclaredField("CONFIGS");
        configManager$mod_config_classes.setAccessible(true);
        configManager$configs.setAccessible(true);
        Map<String, Set<Class<?>>> MOD_CONFIG_CLASSES = (Map<String, Set<Class<?>>>) configManager$mod_config_classes.get(null);
        Map<String, Configuration> CONFIGS = (Map<String, Configuration>) configManager$configs.get(null);

        Config config = configClass.getAnnotation(Config.class);
        String modId = config.modid();

        Set<Class<?>> modConfigClasses = MOD_CONFIG_CLASSES.computeIfAbsent(modId, k -> Sets.newHashSet());
        modConfigClasses.add(configClass);

        File configDir = new File(Launch.minecraftHome, "config");
        File configFile = new File(configDir, config.name() + ".cfg");
        Configuration cfg = CONFIGS.get(configFile.getAbsolutePath());
        if (cfg == null) {
            cfg = new Configuration(configFile);
            cfg.load();
            CONFIGS.put(configFile.getAbsolutePath(), cfg);
        }

        CONFIGMANAGER$SYNC.invokeExact(cfg, configClass, modId, config.category(), true, (Object) null);

        cfg.save();
    }

}
