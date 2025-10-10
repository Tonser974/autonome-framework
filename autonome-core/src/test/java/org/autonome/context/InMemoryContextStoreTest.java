// autonome-core/src/test/java/org/autonome/context/InMemoryContextStoreTest.java
package org.autonome.context;

import static org.junit.jupiter.api.Assertions.*;

import org.autonome.agentcore.AgentContextStore;
import org.autonome.agentcore.InMemoryContextStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class InMemoryContextStoreTest {

    private AgentContextStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryContextStore();
    }

    @Test
    @DisplayName("Should save and retrieve context")
    void testSaveAndRetrieve() {
        String tenantId = "tenant1";
        String conversationId = "conv1";
        
        AgentContext context = new AgentContext(tenantId, conversationId);
        context.put("key", "value");
        
        store.save(tenantId, conversationId, context);
        AgentContext retrieved = store.getOrCreate(tenantId, conversationId);

        assertNotNull(retrieved);
        assertEquals("value", retrieved.get("key"));
    }

    @Test
    @DisplayName("Should create new context if not exists")
    void testGetOrCreateNew() {
        AgentContext result = store.getOrCreate("tenant1", "new-conv");
        
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("Should update existing context")
    void testUpdateContext() {
        String tenantId = "tenant1";
        String conversationId = "conv1";
        
        AgentContext context1 = new AgentContext(tenantId, conversationId);
        context1.put("key", "value1");
        store.save(tenantId, conversationId, context1);

        AgentContext context2 = new AgentContext(tenantId, conversationId);
        context2.put("key", "value2");
        store.save(tenantId, conversationId, context2);

        AgentContext retrieved = store.getOrCreate(tenantId, conversationId);
        assertEquals("value2", retrieved.get("key"));
    }

    @Test
    @DisplayName("Should isolate contexts by tenant")
    void testTenantIsolation() {
        AgentContext context1 = new AgentContext("tenant1", "conv1");
        context1.put("data", "tenant1-data");
        
        AgentContext context2 = new AgentContext("tenant2", "conv1");
        context2.put("data", "tenant2-data");

        store.save("tenant1", "conv1", context1);
        store.save("tenant2", "conv1", context2);

        AgentContext retrieved1 = store.getOrCreate("tenant1", "conv1");
        AgentContext retrieved2 = store.getOrCreate("tenant2", "conv1");
        
        assertEquals("tenant1-data", retrieved1.get("data"));
        assertEquals("tenant2-data", retrieved2.get("data"));
    }

    @Test
    @DisplayName("Should isolate contexts by conversation")
    void testConversationIsolation() {
        AgentContext context1 = new AgentContext("tenant1", "conv1");
        context1.put("data", "conv1-data");
        
        AgentContext context2 = new AgentContext("tenant1", "conv2");
        context2.put("data", "conv2-data");

        store.save("tenant1", "conv1", context1);
        store.save("tenant1", "conv2", context2);

        AgentContext retrieved1 = store.getOrCreate("tenant1", "conv1");
        AgentContext retrieved2 = store.getOrCreate("tenant1", "conv2");
        
        assertEquals("conv1-data", retrieved1.get("data"));
        assertEquals("conv2-data", retrieved2.get("data"));
    }

    @Test
    @DisplayName("Should delete conversation context")
    void testDeleteConversation() {
        String tenantId = "tenant1";
        String conversationId = "conv1";
        
        AgentContext context = new AgentContext(tenantId, conversationId);
        context.put("key", "value");
        store.save(tenantId, conversationId, context);

        store.delete(tenantId, conversationId);

        AgentContext retrieved = store.getOrCreate(tenantId, conversationId);
        assertTrue(retrieved.getData().isEmpty()); // New empty context
    }

    @Test
    @DisplayName("Should clear all tenant contexts")
    void testClearTenant() {
        String tenantId = "tenant1";
        
        AgentContext context1 = new AgentContext(tenantId, "conv1");
        context1.put("key", "value1");
        AgentContext context2 = new AgentContext(tenantId, "conv2");
        context2.put("key", "value2");
        
        store.save(tenantId, "conv1", context1);
        store.save(tenantId, "conv2", context2);

        store.clearTenant(tenantId);

        AgentContext retrieved1 = store.getOrCreate(tenantId, "conv1");
        AgentContext retrieved2 = store.getOrCreate(tenantId, "conv2");
        
        assertTrue(retrieved1.getData().isEmpty());
        assertTrue(retrieved2.getData().isEmpty());
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void testThreadSafety() throws InterruptedException {
        String tenantId = "tenant1";
        String conversationId = "conv1";
        int threadCount = 10;
        int iterationsPerThread = 100;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    AgentContext context = new AgentContext(tenantId, conversationId);
                    context.put("thread", threadId);
                    context.put("iteration", j);
                    store.save(tenantId, conversationId, context);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        AgentContext result = store.getOrCreate(tenantId, conversationId);
        assertNotNull(result);
        assertNotNull(result.get("thread"));
        assertNotNull(result.get("iteration"));
    }
}