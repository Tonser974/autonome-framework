package org.autonome.agentcore.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.autonome.agentcore.executor.AgentExecutor;
import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.autonome.context.AgentContext;
import org.springframework.stereotype.Component;

@Component
public class ParallelFlowEngine extends AbstractFlowEngine {

    public ParallelFlowEngine(AgentExecutor agentExecutor, FlowLoader flowLoader, FlowEngineRegistry engineRegistry) {
        super(agentExecutor, flowLoader, engineRegistry);
    }

    @Override
    protected void doExecute(Flow flow, AgentContext context) {
        ExecutorService executor = Executors.newFixedThreadPool(flow.getTasks().size());
        List<Future<?>> futures = new ArrayList<>();

        for (Task task : flow.getTasks()) {
            if (shouldSkipTask(task, flow, context)) continue;

            List<Object> items = resolveLoopItems(task, context);
            if (items != null) {
                for (Object item : items) {
                    futures.add(executor.submit(() -> {
                        context.put("loop_item", item);
                        executeTaskOrSubflow(task, context);
                    }));
                }
                continue;
            }

            futures.add(executor.submit(() -> executeTaskOrSubflow(task, context)));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Parallel task execution error", e);
            }
        }

        executor.shutdown();
    }

    @Override
    public String getType() {
        return "parallel";
    }
}
