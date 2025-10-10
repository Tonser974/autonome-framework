package org.autonome.agentcore.executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.autonome.agentcore.engine.FlowEngine;
import org.autonome.agentcore.engine.FlowEngineRegistry;
import org.autonome.api.Flow;
import org.autonome.context.AgentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Unit tests for FlowExecutor.
 * Tests flow orchestration and engine selection without any community dependencies.
 */
class FlowExecutorTest {

    @Mock
    private FlowEngineRegistry engineRegistry;
    
    @Mock
    private FlowEngine mockEngine;
    
    private FlowExecutor flowExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        flowExecutor = new FlowExecutor(engineRegistry);
    }

    @Test
    @DisplayName("Should select sequential engine for sequential flow")
    void testSequentialEngineSelection() {
        // Given: A sequential flow
        Flow flow = Flow.builder()
            .id("test-flow")
            .name("Test Sequential Flow")
            .type("sequential")
            .tasks(List.of())
            .build();

        when(engineRegistry.getEngine("sequential")).thenReturn(mockEngine);
        doNothing().when(mockEngine).execute(any(), any());

        // When: Execute the flow
        AgentContext context = new AgentContext("tenant1", "conv1");
        flowExecutor.run(flow, context);

        // Then: Sequential engine was used
        verify(engineRegistry, times(1)).getEngine("sequential");
        verify(mockEngine, times(1)).execute(eq(flow), eq(context));
    }

    @Test
    @DisplayName("Should select parallel engine for parallel flow")
    void testParallelEngineSelection() {
        // Given: A parallel flow
        Flow flow = Flow.builder()
            .id("parallel-flow")
            .type("parallel")
            .tasks(List.of())
            .build();

        when(engineRegistry.getEngine("parallel")).thenReturn(mockEngine);
        doNothing().when(mockEngine).execute(any(), any());

        // When: Execute the flow
        AgentContext context = new AgentContext("tenant1", "conv1");
        flowExecutor.run(flow, context);

        // Then: Parallel engine was used
        verify(engineRegistry, times(1)).getEngine("parallel");
        verify(mockEngine, times(1)).execute(eq(flow), eq(context));
    }

    @Test
    @DisplayName("Should select conversational engine for conversational flow")
    void testConversationalEngineSelection() {
        // Given: A conversational flow
        Flow flow = Flow.builder()
            .id("chat-flow")
            .type("conversational")
            .tasks(List.of())
            .build();

        when(engineRegistry.getEngine("conversational")).thenReturn(mockEngine);
        doNothing().when(mockEngine).execute(any(), any());

        // When: Execute the flow
        AgentContext context = new AgentContext("tenant1", "conv1");
        flowExecutor.run(flow, context);

        // Then: Conversational engine was used
        verify(engineRegistry, times(1)).getEngine("conversational");
        verify(mockEngine, times(1)).execute(eq(flow), eq(context));
    }

    @Test
    @DisplayName("Should pass context to engine correctly")
    void testContextPassthrough() {
        // Given: A flow with initial context data
        Flow flow = Flow.builder()
            .id("test-flow")
            .type("sequential")
            .tasks(List.of())
            .build();

        AgentContext context = new AgentContext("tenant1", "conv1");
        context.put("initial_key", "initial_value");

        when(engineRegistry.getEngine("sequential")).thenReturn(mockEngine);
        doNothing().when(mockEngine).execute(any(), any());

        // When: Execute the flow
        flowExecutor.run(flow, context);

        // Then: Same context instance was passed to engine
        verify(mockEngine).execute(eq(flow), eq(context));
        assertEquals("initial_value", context.get("initial_key"));
    }

    @Test
    @DisplayName("Should handle engine execution exceptions")
    void testEngineExecutionException() {
        // Given: An engine that throws exception
        Flow flow = Flow.builder()
            .id("failing-flow")
            .type("sequential")
            .tasks(List.of())
            .build();

        when(engineRegistry.getEngine("sequential")).thenReturn(mockEngine);
        doThrow(new RuntimeException("Engine execution failed"))
            .when(mockEngine).execute(any(), any());

        // When/Then: Exception should propagate
        AgentContext context = new AgentContext("tenant1", "conv1");
        assertThrows(RuntimeException.class, () -> {
            flowExecutor.run(flow, context);
        });

        verify(engineRegistry, times(1)).getEngine("sequential");
        verify(mockEngine, times(1)).execute(any(), any());
    }

    @Test
    @DisplayName("Should handle missing engine gracefully")
    void testMissingEngine() {
        // Given: A flow type with no registered engine
        Flow flow = Flow.builder()
            .id("unknown-flow")
            .type("unknown-type")
            .tasks(List.of())
            .build();

        when(engineRegistry.getEngine("unknown-type"))
            .thenThrow(new IllegalArgumentException("No engine for type: unknown-type"));

        // When/Then: Should throw exception
        AgentContext context = new AgentContext("tenant1", "conv1");
        assertThrows(IllegalArgumentException.class, () -> {
            flowExecutor.run(flow, context);
        });
    }

    @Test
    @DisplayName("Should execute multiple flows with same context")
    void testMultipleFlowsWithSameContext() {
        // Given: Multiple flows
        Flow flow1 = Flow.builder().id("flow-1").type("sequential").tasks(List.of()).build();
        Flow flow2 = Flow.builder().id("flow-2").type("sequential").tasks(List.of()).build();

        when(engineRegistry.getEngine("sequential")).thenReturn(mockEngine);
        doNothing().when(mockEngine).execute(any(), any());

        // When: Execute both flows with same context
        AgentContext context = new AgentContext("tenant1", "conv1");
        context.put("shared_data", "test");
        
        flowExecutor.run(flow1, context);
        flowExecutor.run(flow2, context);

        // Then: Both flows executed
        verify(mockEngine, times(2)).execute(any(), eq(context));
        assertEquals("test", context.get("shared_data"));
    }
}
