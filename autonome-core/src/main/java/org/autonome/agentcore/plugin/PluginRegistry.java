package org.autonome.agentcore.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for dynamically loading agent plugins from JAR files.
 * Scans a plugins directory and creates classloaders for each JAR.
 */
public class PluginRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);
    
    private final List<PluginClassLoader> classLoaders = new ArrayList<>();

    public PluginRegistry(String pluginsDir) {
        File folder = new File(pluginsDir);
        if (!folder.exists() || !folder.isDirectory()) {
            logger.warn("⚠️ Plugins directory does not exist: {}", pluginsDir);
            return;
        }

        File[] jars = folder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars != null) {
            for (File jar : jars) {
                try {
                    classLoaders.add(new PluginClassLoader(jar, Thread.currentThread().getContextClassLoader()));
                    logger.info("🧩 Plugin loaded: {}", jar.getName());
                } catch (Exception e) {
                    logger.error("❌ Failed to load plugin jar: {} - {}", jar.getName(), e.getMessage());
                }
            }
        }
        
        logger.info("✅ Loaded {} plugin(s) from: {}", classLoaders.size(), pluginsDir);
    }

    public List<PluginClassLoader> getClassLoaders() {
        return classLoaders;
    }
}