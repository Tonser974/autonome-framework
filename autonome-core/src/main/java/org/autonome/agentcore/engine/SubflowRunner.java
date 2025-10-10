package org.autonome.agentcore.engine;

import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.autonome.context.AgentContext;

public class SubflowRunner {

    public static void executeSubflow(Task task, AgentContext context, FlowLoader flowLoader, FlowEngineRegistry engineRegistry) {
        try {
            Flow subflow = flowLoader.loadFromYaml(task.getFlowRef());
            FlowEngine engine = engineRegistry.getEngine(subflow.getType());
            engine.execute(subflow, context);

            if (task.getOutputKey() != null && !subflow.getTasks().isEmpty()) {
                Task last = subflow.getTasks().get(subflow.getTasks().size() - 1);
                Object val = context.get(last.getOutputKey());
                if (val != null) {
                    context.put(task.getOutputKey(), val);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute subflow for task [" + task.getId() + "]", e);
        }
    }
} 
