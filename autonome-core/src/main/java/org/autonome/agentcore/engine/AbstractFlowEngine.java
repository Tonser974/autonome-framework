package org.autonome.agentcore.engine;

import java.util.List;

import org.autonome.agentcore.executor.AgentExecutor;
import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all flow execution engines.
 * Provides common functionality for:
 * - Loading global context variables
 * - Evaluating task conditions
 * - Handling loops
 * - Executing subflows
 * - Managing optional tasks
 */
public abstract class AbstractFlowEngine implements FlowEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractFlowEngine.class);
    
    private final AgentExecutor agentExecutor;
    private final FlowLoader flowLoader;
    private final FlowEngineRegistry engineRegistry;

    public AbstractFlowEngine(AgentExecutor agentExecutor, 
                             FlowLoader flowLoader, 
                             FlowEngineRegistry engineRegistry) {
        this.agentExecutor = agentExecutor;
        this.flowLoader = flowLoader;
        this.engineRegistry = engineRegistry;
    }

    @Override
    public final void execute(Flow flow, AgentContext context) {
        if (flow.getGlobals() != null) {
            context.putAll(flow.getGlobals());
            logger.debug("🔵 Context after loading globals: {}", context);
        }
        doExecute(flow, context);
    }

    protected boolean shouldSkipTask(Task task, Flow flow, AgentContext context) {
        String condition = task.getCondition();
        if (condition != null && !condition.trim().isEmpty()) {
            logger.debug("🧪 Evaluating condition: [{}]", condition);
            boolean result = ConditionEvaluator.evaluate(condition.trim(), context, flow.getGlobals());
            if (!result) {
                logger.info("⚠️ Skipping task [{}] due to unmet condition: {}", task.getId(), condition);
            }
            return !result;
        }
        return false;
    }

    protected List<Object> resolveLoopItems(Task task, AgentContext context) {
    String loopOver = task.getLoopOver();
    
    // If no loop is configured, return null (execute task once)
    if (loopOver == null || loopOver.trim().isEmpty()) {
        return null;
    }
    
    Object collection = context.get(loopOver);
    if (collection instanceof List<?> list) {
        return List.copyOf(list);
    }
    
    logger.warn("⚠️ Loop variable '{}' is not a List, skipping loop", loopOver);
    return null;
}

    protected void executeTaskOrSubflow(Task task, AgentContext context) {
        try {
            if (task.getFlowRef() != null && !task.getFlowRef().isEmpty()) {
                logger.info("🔁 Executing subflow: {}", task.getFlowRef());
                SubflowRunner.executeSubflow(task, context, flowLoader, engineRegistry);
            } else {
                logger.info("🚀 Executing task: {}", task.getId());
                agentExecutor.execute(task, context);
            }
        } catch (Exception e) {
            if (task.isOptional()) {
                logger.warn("⚠️ Optional task [{}] failed: {}", task.getId(), e.getMessage());
            } else {
                logger.error("❌ Task [{}] failed: {}", task.getId(), e.getMessage());
                throw new RuntimeException("Task [" + task.getId() + "] failed", e);
            }
        }
    }

    protected abstract void doExecute(Flow flow, AgentContext context);
}