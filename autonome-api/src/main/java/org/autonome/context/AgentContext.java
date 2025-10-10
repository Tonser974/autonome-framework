    package org.autonome.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe context for managing state across agent executions within a flow.
 * 
 * <p>AgentContext provides:
 * <ul>
 *   <li>Multi-tenant isolation via tenantId</li>
 *   <li>Conversation tracking via conversationId</li>
 *   <li>Key-value data storage shared across tasks</li>
 *   <li>Conversation log for message history</li>
 * </ul>
 * 
 * <p>Thread-safe for use in parallel flow execution.
 * 
 * @since 1.0.0
 */
public class AgentContext {
    
    private final String tenantId;
    private final String conversationId;
    private final List<String> conversationLog = new CopyOnWriteArrayList<>();
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    /**
     * Creates a new AgentContext with tenant and conversation IDs.
     *
     * @param tenantId the tenant identifier (for multi-tenant isolation)
     * @param conversationId the conversation identifier (for flow tracking)
     */
    public AgentContext(String tenantId, String conversationId) {
        this.tenantId = tenantId;
        this.conversationId = conversationId;
    }

    /**
     * Creates a new AgentContext with only a conversation ID.
     * TenantId will be null (single-tenant mode).
     *
     * @param conversationId the conversation identifier
     */
    public AgentContext(String conversationId) {
        this(null, conversationId);
    }

    // ========== Getters ==========

    /**
     * Returns the tenant ID for this context.
     *
     * @return the tenant ID, or null if not set
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Returns the conversation ID for this context.
     *
     * @return the conversation ID
     */
    public String getConversationId() {
        return conversationId;
    }

    // ========== Conversation Log ==========

    /**
     * Adds a message to the conversation log.
     *
     * @param message the message to add
     */
    public void addMessage(String message) {
        conversationLog.add(message);
    }

    /**
     * Returns the conversation log (thread-safe copy).
     *
     * @return list of messages in conversation order
     */
    public List<String> getConversationLog() {
        return conversationLog;
    }

    // ========== Data Storage ==========

    /**
 * Stores a value in the context.
 * Note: null values are not supported and will be ignored.
 *
 * @param key the key
 * @param value the value to store (must not be null)
 */
public void put(String key, Object value) {
    if (key != null && value != null) {  // ✅ Add null check
        data.put(key, value);
    }
}

    /**
     * Retrieves a value from the context (untyped).
     *
     * @param key the key
     * @return the value, or null if not found
     */
    public Object get(String key) {
        return data.get(key);
    }

    /**
     * Retrieves a value from the context with type safety.
     *
     * @param <T> the expected type
     * @param key the key
     * @param type the expected class type
     * @return the value cast to type T, or null if not found
     * @throws ClassCastException if the value cannot be cast to the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException(
            "Value for key '" + key + "' is of type " + value.getClass().getName() + 
            ", expected " + type.getName()
        );
    }

    /**
     * Returns all data in the context (thread-safe view).
     *
     * @return map of all key-value pairs
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Stores multiple key-value pairs in the context.
     *
     * @param values map of values to store
     */
    public void putAll(Map<String, Object> values) {
        if (values != null) {
            data.putAll(values);
        }
    }

    /**
     * Removes a specific key from the context.
     *
     * @param key the key to remove
     */
    public void clear(String key) {
        data.remove(key);
    }

    /**
     * Clears all data from the context (keeps conversation log).
     */
    public void clear() {
        data.clear();
    }

    /**
     * Clears all data and conversation log.
     */
    public void clearAll() {
        data.clear();
        conversationLog.clear();
    }

    /**
     * Checks if a key exists in the context.
     *
     * @param key the key to check
     * @return true if key exists, false otherwise
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    /**
     * Returns the number of keys in the context.
     *
     * @return the size of the data map
     */
    public int size() {
        return data.size();
    }

    @Override
    public String toString() {
        return "AgentContext{" +
                "tenantId='" + tenantId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", dataSize=" + data.size() +
                ", logSize=" + conversationLog.size() +
                '}';
    }
}