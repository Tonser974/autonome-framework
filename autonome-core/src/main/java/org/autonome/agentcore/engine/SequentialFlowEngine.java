package org.autonome.agentcore.engine;

import java.util.List;

import org.autonome.agentcore.executor.AgentExecutor;
import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.autonome.context.AgentContext;
import org.springframework.stereotype.Component;

@Component
public class SequentialFlowEngine extends AbstractFlowEngine {

    public SequentialFlowEngine(AgentExecutor agentExecutor, FlowLoader flowLoader, FlowEngineRegistry engineRegistry) {
        super(agentExecutor, flowLoader, engineRegistry); // ✅ delegated to base class
    }

    @Override
    protected void doExecute(Flow flow, AgentContext context) {
        for (Task task : flow.getTasks()) {
            // ✅ Reuse condition check
            if (shouldSkipTask(task, flow, context)) continue;

            // ✅ Handle loop-over scenarios
            List<Object> items = resolveLoopItems(task, context);
            if (items != null) {
                for (Object item : items) {
                    context.put("loop_item", item);
                    executeTaskOrSubflow(task, context); // ✅ centralized execution
                }
                continue;
            }

            // ✅ Default execution
            executeTaskOrSubflow(task, context);
        }
    }

    @Override
    public String getType() {
        return "sequential";
    }
}
