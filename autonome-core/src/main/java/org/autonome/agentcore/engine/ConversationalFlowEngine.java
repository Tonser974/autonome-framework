package org.autonome.agentcore.engine;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.autonome.agentcore.executor.AgentExecutor;
import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Flow engine for conversational agents that maintain conversation history.
 * Tracks user messages and agent responses to avoid duplicates and
 * builds up conversation context over multiple turns.
 */
@Component
public class ConversationalFlowEngine extends AbstractFlowEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationalFlowEngine.class);
    
    private final AtomicReference<String> lastUserMessage = new AtomicReference<>();
    private final AtomicReference<String> lastAgentResponse = new AtomicReference<>();

    public ConversationalFlowEngine(AgentExecutor agentExecutor, 
                                   FlowLoader flowLoader,
                                   FlowEngineRegistry engineRegistry) {
        super(agentExecutor, flowLoader, engineRegistry);
    }

    @Override
    protected void doExecute(Flow flow, AgentContext context) {
        for (Task task : flow.getTasks()) {
            if (shouldSkipTask(task, flow, context)) {
                continue;
            }

            List<Object> items = resolveLoopItems(task, context);
            if (items != null) {
                for (Object item : items) {
                    context.put("loop_item", item);
                    runConversationalStep(task, context);
                }
                continue;
            }

            runConversationalStep(task, context);
        }
    }

    private void runConversationalStep(Task task, AgentContext context) {
        try {
            // Handle user message memory
            Object userInput = task.getInput().get("user_input");
            if (userInput instanceof String inputStr && !inputStr.trim().startsWith("@")) {
                String normalizedUser = inputStr.trim().toLowerCase();
                if (!normalizedUser.equals(lastUserMessage.get())) {
                    context.addMessage("User: " + inputStr);
                    lastUserMessage.set(normalizedUser);
                    logger.debug("💬 User message added to context");
                }
            }

            // Execute agent or subflow
            executeTaskOrSubflow(task, context);

            // Handle agent response memory
            Object result = context.get(task.getOutputKey());
            if (result != null) {
                String normalizedAgent = result.toString().trim().toLowerCase();
                if (!normalizedAgent.equals(lastAgentResponse.get())) {
                    context.addMessage("Agent: " + result);
                    lastAgentResponse.set(normalizedAgent);
                    logger.debug("🤖 Agent response added to context");
                }
            }
        } catch (Exception e) {
            if (task.isOptional()) {
                logger.warn("⚠️ Optional conversational task [{}] failed: {}", task.getId(), e.getMessage());
            } else {
                logger.error("❌ Conversational task [{}] failed: {}", task.getId(), e.getMessage());
                throw new RuntimeException("Conversational task [" + task.getId() + "] failed", e);
            }
        }
    }

    @Override
    public String getType() {
        return "conversational";
    }
}