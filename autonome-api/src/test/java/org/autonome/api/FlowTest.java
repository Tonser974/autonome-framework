package org.autonome.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlowTest {

    @Test
    void testFlowBuilder() {
        // Given
        String id = "test-flow";
        String name = "Test Flow";
        String type = "sequential";
        String description = "A test flow";
        Map<String, Object> globals = Map.of("model", "gpt-4");

        // When
        Flow flow = Flow.builder()
                .id(id)
                .name(name)
                .type(type)
                .description(description)
                .globals(globals)
                .build();

        // Then
        assertEquals(id, flow.getId());
        assertEquals(name, flow.getName());
        assertEquals(type, flow.getType());
        assertEquals(description, flow.getDescription());
        assertEquals(globals, flow.getGlobals());
        assertNull(flow.getTasks()); // Not set, should be null
    }

    @Test
    void testFlowConstructor() {
        // Given
        String id = "test-flow";
        String name = "Test Flow";
        String type = "sequential";
        String description = "A test flow";
        List<Task> tasks = List.of(); // Empty list
        Map<String, Object> globals = Map.of("key", "value");

        // When
        Flow flow = new Flow(id, name, type, description, tasks, globals);

        // Then
        assertEquals(id, flow.getId());
        assertEquals(name, flow.getName());
        assertEquals(type, flow.getType());
        assertEquals(description, flow.getDescription());
        assertEquals(tasks, flow.getTasks());
        assertEquals(globals, flow.getGlobals());
    }

    @Test
    void testFlowSetters() {
        // Given
        Flow flow = new Flow();

        // When
        flow.setId("new-id");
        flow.setName("New Name");
        flow.setType("parallel");
        flow.setDescription("Updated description");

        // Then
        assertEquals("new-id", flow.getId());
        assertEquals("New Name", flow.getName());
        assertEquals("parallel", flow.getType());
        assertEquals("Updated description", flow.getDescription());
    }

    @Test
    void testFlowEqualsAndHashCode() {
        // Given
        Flow flow1 = Flow.builder()
                .id("flow1")
                .name("Flow 1")
                .type("sequential")
                .build();

        Flow flow2 = Flow.builder()
                .id("flow1")
                .name("Flow 1")
                .type("sequential")
                .build();

        Flow flow3 = Flow.builder()
                .id("flow2")
                .name("Flow 2")
                .type("parallel")
                .build();

        // Then
        assertEquals(flow1, flow2); // Same data = equal
        assertNotEquals(flow1, flow3); // Different data = not equal
        assertEquals(flow1.hashCode(), flow2.hashCode()); // Same hashcode
    }

    @Test
    void testFlowToString() {
        // Given
        Flow flow = Flow.builder()
                .id("test-flow")
                .name("Test")
                .build();

        // When
        String result = flow.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test-flow"));
        assertTrue(result.contains("Test"));
    }
}