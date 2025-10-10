package org.autonome.agentcore.executor;

import org.autonome.agentcore.engine.FlowEngine;
import org.autonome.agentcore.engine.FlowEngineRegistry;
import org.autonome.api.Flow;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Orchestrates flow execution by selecting the appropriate engine
 * based on flow type (sequential, parallel, conversational, etc.)
 */
@Component
public class FlowExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowExecutor.class);
    
    private final FlowEngineRegistry engineRegistry;

    public FlowExecutor(FlowEngineRegistry engineRegistry) {
        this.engineRegistry = engineRegistry;
    }

    public void run(Flow flow, AgentContext context) {
        FlowEngine engine = engineRegistry.getEngine(flow.getType());
        logger.info("▶ Executing flow [{}] with type: {}", flow.getId(), flow.getType());
        logger.debug("▶ Using engine: {}", engine.getClass().getSimpleName());
        engine.execute(flow, context);
    }
}