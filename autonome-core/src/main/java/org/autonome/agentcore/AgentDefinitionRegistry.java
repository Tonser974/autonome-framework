package org.autonome.agentcore;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AgentDefinitionRegistry {
    private final Map<String, AgentDefinition> definitions = new HashMap<>();

    public void registerAll(List<AgentDefinition> defs) {
        defs.forEach(def -> definitions.put(def.getAgentId(), def));
    }

    public AgentDefinition get(String agentId) {
        return definitions.get(agentId);
    }

    public Collection<AgentDefinition> getAll() {
        return definitions.values();
    }
}
