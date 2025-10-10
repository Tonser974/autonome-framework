package org.autonome.agentcore;

import java.util.List;

public interface AgentInitializer {
    void init(AgentDefinition definition, List<AgentExtension> extensions);
}