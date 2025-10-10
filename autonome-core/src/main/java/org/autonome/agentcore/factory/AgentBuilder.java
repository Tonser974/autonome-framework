package org.autonome.agentcore.factory;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.api.Agent;

public interface AgentBuilder {
    boolean supports(String type);
    Agent build(AgentDefinition def);
}
