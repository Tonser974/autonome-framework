package org.autonome.agentcore;

import org.autonome.context.AgentContext;

/**
 * Storage abstraction for agent execution contexts.
 * Supports multi-tenant, conversation-based context management.
 * 
 * <p><b>Implementations:</b></p>
 * <ul>
 *   <li>InMemoryContextStore - Default for development</li>
 *   <li>PostgresContextStore - Production persistence</li>
 *   <li>Custom stores - Implement this interface for Redis, MongoDB, etc.</li>
 * </ul>
 * 
 * <p><b>Thread Safety:</b> Implementations must be thread-safe.</p>
 */
public interface AgentContextStore {
    
    /**
     * Retrieves an existing context or creates a new one.
     * 
     * @param tenantId Tenant identifier for multi-tenancy
     * @param conversationId Unique conversation identifier
     * @return AgentContext instance (never null)
     */
    AgentContext getOrCreate(String tenantId, String conversationId);
    
    /**
     * Persists the context to storage.
     * 
     * @param tenantId Tenant identifier
     * @param conversationId Conversation identifier
     * @param context Context to save
     */
    void save(String tenantId, String conversationId, AgentContext context);
    
    /**
     * Deletes a specific conversation context.
     * 
     * @param tenantId Tenant identifier
     * @param conversationId Conversation identifier
     */
    void delete(String tenantId, String conversationId);
    
    /**
     * Clears all contexts for a tenant.
     * Useful for tenant cleanup or compliance requirements.
     * 
     * @param tenantId Tenant identifier
     */
    void clearTenant(String tenantId);

    void saveAsync(String tenantId, String conversationId, AgentContext context); 
}
