package org.autonome.agentcore.factory;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.extensions.python.PythonAgent;
import org.autonome.api.Agent;
import org.springframework.stereotype.Component;

@Component
public class PythonAgentBuilder implements AgentBuilder {

    @Override
    public boolean supports(String type) {
        return "python".equalsIgnoreCase(type);
    }

    @Override
    public Agent build(AgentDefinition def) {
        return new PythonAgent(def);
    }
}
