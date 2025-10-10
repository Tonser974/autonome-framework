package org.autonome.agentcore.factory;

import java.util.List;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.api.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultAgentFactory implements AgentFactory {

    @Autowired
    private List<AgentBuilder> builders;

    @Override
    public Agent createAgent(AgentDefinition def) {
        return builders.stream()
                .filter(b -> b.supports(def.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No builder for type " + def.getType()))
                .build(def);
    }
}
