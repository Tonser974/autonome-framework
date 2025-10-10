package org.autonome.agentcore.factory;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.extensions.rest.RestAgent;
import org.autonome.api.Agent;
import org.springframework.stereotype.Component;

@Component
public class RestAgentBuilder implements AgentBuilder {

    @Override
    public boolean supports(String type) {
        return "rest".equalsIgnoreCase(type);
    }

    @Override
    public Agent build(AgentDefinition def) {
        return new RestAgent(def);
    }
}
