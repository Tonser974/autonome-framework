package org.autonome.agentcore.factory;

import java.util.List;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.AgentExtension;
import org.autonome.agentcore.AgentExtensionRegistry;
import org.autonome.agentcore.AgentInitializer;
import org.autonome.agentcore.plugin.PluginClassLoader;
import org.autonome.agentcore.plugin.PluginRegistry;
import org.autonome.api.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class JavaAgentBuilder implements AgentBuilder {

    @Value("${autonome.plugins.dir:plugins}")
    private String pluginDir;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AgentExtensionRegistry extensionRegistry;

    private PluginRegistry pluginRegistry;

    @PostConstruct
    public void init() {
        pluginRegistry = new PluginRegistry(pluginDir);
    }

    @Override
    public boolean supports(String type) {
        return "java".equalsIgnoreCase(type);
    }

    @Override
    public Agent build(AgentDefinition def) {
        String className = def.getConfig().get("class");
        System.out.println("Attempting to load agent class: " + className);

        Agent agentInstance = null;
        try {
            Class<?> agentClass = Class.forName(className);
            agentInstance = (Agent) context.getBean(agentClass);

            // Corrected method call here:
            if (agentInstance instanceof AgentInitializer) {
                // Pass def.getEnabledExtensions() (which is List<String>) to the registry method
                List<AgentExtension> extensions = extensionRegistry.getEnabledExtensions(def.getEnabledExtensions());
                ((AgentInitializer) agentInstance).init(def, extensions);
            }
            return agentInstance;

        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            System.err.println("Class not found on main application classpath: " + className + ". Trying plugin classloaders. Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error getting bean from Spring context for class: " + className + ". Error: " + e.getMessage());
            throw new RuntimeException("Failed to get Spring bean for agent: " + className, e);
        }

        // Fallback to plugin classloader if not found as a Spring bean
        for (PluginClassLoader loader : pluginRegistry.getClassLoaders()) {
            try {
                Class<?> pluginClass = Class.forName(className, true, loader);
                agentInstance = (Agent) pluginClass.getDeclaredConstructor().newInstance();
                Thread.currentThread().setContextClassLoader(loader);

                // Corrected method call here for plugin-loaded agents too
                if (agentInstance instanceof AgentInitializer) {
                    // Pass def.getEnabledExtensions() (which is List<String>) to the registry method
                    List<AgentExtension> extensions = extensionRegistry.getEnabledExtensions(def.getEnabledExtensions());
                    ((AgentInitializer) agentInstance).init(def, extensions);
                }
                return agentInstance;
            } catch (Exception e) {
                System.err.println("Error loading agent from plugin classloader: " + className + ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException("❌ Could not find Agent class: " + className);
    }
}