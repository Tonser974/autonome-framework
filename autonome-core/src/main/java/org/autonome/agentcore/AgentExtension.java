package org.autonome.agentcore;

import org.autonome.context.AgentContext;

public interface AgentExtension {
    String getName();
    Object execute(String input, AgentContext context) throws Exception;
}
