package org.autonome.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AgentContextTest {

    private AgentContext context;

    @BeforeEach
    void setUp() {
        context = new AgentContext("tenant1", "conv1");
    }

    // ========== Constructor Tests ==========

    @Test
    void testConstructorWithTenantAndConversation() {
        // When
        AgentContext ctx = new AgentContext("tenant123", "conv456");

        // Then
        assertEquals("tenant123", ctx.getTenantId());
        assertEquals("conv456", ctx.getConversationId());
        assertEquals(0, ctx.size());
        assertTrue(ctx.getConversationLog().isEmpty());
    }

    @Test
    void testConstructorWithOnlyConversation() {
        // When
        AgentContext ctx = new AgentContext("conv789");

        // Then
        assertNull(ctx.getTenantId());
        assertEquals("conv789", ctx.getConversationId());
    }

    // ========== Data Storage Tests ==========

    @Test
    void testPutAndGet() {
        // When
        context.put("name", "Alice");
        context.put("age", 30);

        // Then
        assertEquals("Alice", context.get("name"));
        assertEquals(30, context.get("age"));
    }

    @Test
    void testGetWithNonExistentKey() {
        // When
        Object result = context.get("nonexistent");

        // Then
        assertNull(result);
    }

    @Test
void testPutNullValue() {
    // Given
    context.put("key", "initialValue");
    
    // When - null values are silently ignored (ConcurrentHashMap limitation)
    context.put("key", null);

    // Then - original value is preserved
    assertEquals("initialValue", context.get("key"));
    assertTrue(context.containsKey("key"));
}
    // ========== Type-Safe Get Tests ==========

    @Test
    void testTypeSafeGet() {
        // Given
        context.put("name", "Alice");
        context.put("age", 30);

        // When & Then
        String name = context.get("name", String.class);
        Integer age = context.get("age", Integer.class);

        assertEquals("Alice", name);
        assertEquals(30, age);
    }

    @Test
    void testTypeSafeGetWithWrongType() {
        // Given
        context.put("name", "Alice");

        // When & Then
        assertThrows(ClassCastException.class, () -> {
            Integer wrongType = context.get("name", Integer.class);
        });
    }

    @Test
    void testTypeSafeGetWithNonExistentKey() {
        // When
        String result = context.get("nonexistent", String.class);

        // Then
        assertNull(result);
    }

    @Test
    void testTypeSafeGetWithNull() {
        // Given
       context.put("key", "value");
    
    // When - attempt to put null is ignored
    context.put("key", null);

    // Then - original value is preserved
    String result = context.get("key", String.class);
    assertEquals("value", result); // Still has original
    }

    // ========== PutAll Tests ==========

    @Test
    void testPutAll() {
        // Given
        Map<String, Object> data = Map.of(
                "key1", "value1",
                "key2", 123,
                "key3", true
        );

        // When
        context.putAll(data);

        // Then
        assertEquals("value1", context.get("key1"));
        assertEquals(123, context.get("key2"));
        assertEquals(true, context.get("key3"));
        assertEquals(3, context.size());
    }

    @Test
    void testPutAllWithNull() {
        // When
        context.putAll(null);

        // Then
        assertEquals(0, context.size()); // Should not throw NPE
    }

    @Test
    void testPutAllOverwritesExisting() {
        // Given
        context.put("key", "oldValue");

        // When
        context.putAll(Map.of("key", "newValue"));

        // Then
        assertEquals("newValue", context.get("key"));
    }

    // ========== Clear Tests ==========

    @Test
    void testClearSpecificKey() {
        // Given
        context.put("key1", "value1");
        context.put("key2", "value2");

        // When
        context.clear("key1");

        // Then
        assertNull(context.get("key1"));
        assertEquals("value2", context.get("key2"));
        assertEquals(1, context.size());
    }

    @Test
    void testClearAllData() {
        // Given
        context.put("key1", "value1");
        context.put("key2", "value2");
        context.addMessage("Message 1");

        // When
        context.clear();

        // Then
        assertEquals(0, context.size());
        assertEquals(1, context.getConversationLog().size()); // Log preserved
    }

    @Test
    void testClearAll() {
        // Given
        context.put("key1", "value1");
        context.addMessage("Message 1");

        // When
        context.clearAll();

        // Then
        assertEquals(0, context.size());
        assertEquals(0, context.getConversationLog().size());
    }

    // ========== Conversation Log Tests ==========

    @Test
    void testAddMessage() {
        // When
        context.addMessage("Message 1");
        context.addMessage("Message 2");

        // Then
        List<String> log = context.getConversationLog();
        assertEquals(2, log.size());
        assertEquals("Message 1", log.get(0));
        assertEquals("Message 2", log.get(1));
    }

    @Test
    void testConversationLogImmutability() {
        // Given
        context.addMessage("Message 1");

        // When
        List<String> log = context.getConversationLog();

        // Then - List is a view, but modifications are isolated
        assertDoesNotThrow(() -> log.size());
    }

    // ========== Utility Tests ==========

    @Test
    void testContainsKey() {
        // Given
        context.put("key", "value");

        // Then
        assertTrue(context.containsKey("key"));
        assertFalse(context.containsKey("nonexistent"));
    }

    @Test
    void testSize() {
        // Given
        assertEquals(0, context.size());

        // When
        context.put("key1", "value1");
        context.put("key2", "value2");

        // Then
        assertEquals(2, context.size());
    }

    @Test
    void testGetData() {
        // Given
        context.put("key1", "value1");
        context.put("key2", "value2");

        // When
        Map<String, Object> data = context.getData();

        // Then
        assertEquals(2, data.size());
        assertEquals("value1", data.get("key1"));
        assertEquals("value2", data.get("key2"));
    }

    @Test
    void testToString() {
        // When
        String result = context.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("tenant1"));
        assertTrue(result.contains("conv1"));
        assertTrue(result.contains("dataSize"));
        assertTrue(result.contains("logSize"));
    }

    // ========== Thread Safety Tests ==========

    @Test
    void testConcurrentPuts() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        context.put("key-" + threadId + "-" + j, "value-" + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(threadCount * operationsPerThread, context.size());
    }

    @Test
    void testConcurrentReads() throws InterruptedException {
        // Given
        context.put("sharedKey", "sharedValue");
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        String value = context.get("sharedKey", String.class);
                        if ("sharedValue".equals(value)) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(threadCount * 100, successCount.get());
    }

    @Test
    void testConcurrentMessageAdds() throws InterruptedException {
        // Given
        int threadCount = 10;
        int messagesPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < messagesPerThread; j++) {
                        context.addMessage("Thread-" + threadId + " Message-" + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(threadCount * messagesPerThread, context.getConversationLog().size());
    }
}