package org.autonome.agentcore.factory;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.api.Agent;

public interface AgentFactory {
    Agent createAgent(AgentDefinition def);
}
