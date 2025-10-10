// autonome-agentcore/src/main/java/org/autonome/agentcore/AgentExtensionRegistry.java
package org.autonome.agentcore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class AgentExtensionRegistry {

    private final Map<String, AgentExtension> registry = new HashMap<>();

    public AgentExtensionRegistry(List<AgentExtension> extensions) {
        for (AgentExtension ext : extensions) {
            registry.put(ext.getName(), ext);
        }
    }

    public Optional<AgentExtension> get(String name) {
        return Optional.ofNullable(registry.get(name));
    }

    public List<AgentExtension> getAll() {
        return List.copyOf(registry.values());
    }

    // Corrected signature for getEnabledExtensions
    // It takes a List<String> of extension names, not a single agentId
    public List<AgentExtension> getEnabledExtensions(List<String> enabledExtensionNames) { // REMOVED String agentId, ADDED enabledExtensionNames as argument
        if (enabledExtensionNames == null || enabledExtensionNames.isEmpty()) {
            return List.of(); // Return empty list if no extensions are enabled
        }
        return enabledExtensionNames.stream()
                .map(this::get) // Get Optional<AgentExtension> for each name
                .filter(Optional::isPresent) // Filter out extensions that were not found
                .map(Optional::get) // Unwrap the Optional
                .collect(Collectors.toList());
    }
}