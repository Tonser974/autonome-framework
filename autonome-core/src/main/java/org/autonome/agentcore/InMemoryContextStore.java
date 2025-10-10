package org.autonome.agentcore;

import java.util.concurrent.ConcurrentHashMap;

import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Default in-memory context store for development and testing.
 * 
 * <p><b>Characteristics:</b></p>
 * <ul>
 *   <li>Thread-safe using ConcurrentHashMap</li>
 *   <li>No persistence - data lost on restart</li>
 *   <li>Fast for single-instance deployments</li>
 *   <li>Auto-disabled if another AgentContextStore bean is provided</li>
 * </ul>
 * 
 * <p><b>Production Use:</b> Not recommended for multi-instance or stateful applications.
 * Use PostgresContextStore or a distributed cache (Redis) instead.</p>
 * 
 * <p><b>Automatic Configuration:</b> This bean is only created if no other 
 * AgentContextStore implementation is found. To use a different store, simply 
 * add it to your classpath and configure it.</p>
 */
@Component
@ConditionalOnMissingBean(AgentContextStore.class)
public class InMemoryContextStore implements AgentContextStore {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryContextStore.class);
    
    private final ConcurrentHashMap<String, AgentContext> registry = new ConcurrentHashMap<>();

    public InMemoryContextStore() {
        logger.info("✅ Using InMemoryContextStore (development mode - not persisted)");
    }

    private String key(String tenantId, String conversationId) {
        return tenantId + ":" + conversationId;
    }

    @Override
    public AgentContext getOrCreate(String tenantId, String conversationId) {
        return registry.computeIfAbsent(key(tenantId, conversationId), 
                                       k -> new AgentContext(tenantId, conversationId));
    }

    @Override
    public void save(String tenantId, String conversationId, AgentContext context) {
        registry.put(key(tenantId, conversationId), context);
        logger.debug("Saved context for {}:{}", tenantId, conversationId);
    }

    @Override
    public void delete(String tenantId, String conversationId) {
        registry.remove(key(tenantId, conversationId));
        logger.debug("Deleted context for {}:{}", tenantId, conversationId);
    }

    @Override
    public void clearTenant(String tenantId) {
        int cleared = (int) registry.keySet().stream()
            .filter(k -> k.startsWith(tenantId + ":"))
            .peek(registry::remove)
            .count();
        logger.info("Cleared {} contexts for tenant: {}", cleared, tenantId);
    }

     @Override
    public void saveAsync(String tenantId, String conversationId, AgentContext context) {
        save(tenantId, conversationId, context);
    }
}