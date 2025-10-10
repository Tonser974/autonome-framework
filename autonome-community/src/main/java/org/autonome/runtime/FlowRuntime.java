package org.autonome.runtime;

import java.util.Map;

import org.autonome.agentcore.AgentContextStore;
import org.autonome.agentcore.executor.FlowExecutor;
import org.autonome.api.Flow;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FlowRuntime orchestrates flow execution with context management
 * Handles tenant isolation and conversation state persistence
 */
@Component
public class FlowRuntime {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowRuntime.class);
    
    private final FlowExecutor flowExecutor;
    private final AgentContextStore contextStore;
    
    public FlowRuntime(
            FlowExecutor executor,
            AgentContextStore contextStore) {
        this.flowExecutor = executor;
        this.contextStore = contextStore;
    }
    
    /**
     * Execute a flow with the given context data
     * 
     * @param tenantId Tenant identifier for isolation
     * @param conversationId Unique conversation identifier
     * @param flowToExecute Flow definition to execute
     * @param globals Global variables to inject into context
     * @return Updated AgentContext after execution
     */
    public AgentContext run(String tenantId, String conversationId, Flow flowToExecute, Map<String, Object> globals) {
        AgentContext ctx = contextStore.getOrCreate(tenantId, conversationId);
        ctx.getData().putAll(globals);
        
        flowExecutor.run(flowToExecute, ctx);
        contextStore.saveAsync(tenantId, conversationId, ctx);
        
        logger.info("Flow executed: {} for conversation: {}", flowToExecute.getName(), conversationId);
        return ctx;
    }
    
    /**
     * Get existing context for a conversation
     * 
     * @param tenantId Tenant identifier
     * @param conversationId Conversation identifier
     * @return AgentContext for the conversation
     */
    public AgentContext getContext(String tenantId, String conversationId) {
        return contextStore.getOrCreate(tenantId, conversationId);
    }
}