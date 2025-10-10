package org.autonome.agentcore.engine;

import org.autonome.api.Flow;
import org.autonome.context.AgentContext;

public interface FlowEngine {
    void execute(Flow flow, AgentContext context);
    String getType(); // e.g., "sequential", "conversational"
}