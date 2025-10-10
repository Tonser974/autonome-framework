package org.autonome.agentcore.executor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.AgentDefinitionRegistry;
import org.autonome.agentcore.factory.AgentFactory;
import org.autonome.agentcore.util.InputResolver;
import org.autonome.api.Agent;
import org.autonome.api.Task;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Executes individual agent tasks by:
 * 1. Resolving agent definition from registry
 * 2. Creating agent instance via factory
 * 3. Resolving task inputs from context
 * 4. Executing agent and storing results
 */
@Component
public class AgentExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentExecutor.class);
    
    private final AgentFactory agentFactory;
    private final AgentDefinitionRegistry definitionRegistry;
    private final CamelContext camelContext;

    public AgentExecutor(AgentFactory agentFactory,
                         AgentDefinitionRegistry definitionRegistry,
                         CamelContext camelContext) {
        this.agentFactory = agentFactory;
        this.definitionRegistry = definitionRegistry;
        this.camelContext = camelContext;
    }

    public void execute(Task task, AgentContext context) {
        try {
            AgentDefinition def = definitionRegistry.get(task.getAgentId());
            Agent agent = agentFactory.createAgent(def);

            Exchange exchange = new DefaultExchange(camelContext);
            InputResolver.resolveIntoExchange(exchange, task.getInput(), context);

            agent.handle(exchange, context);

            Object result = exchange.getMessage().getBody();
            String outputKey = task.getOutputKey();

            if (outputKey != null) {
                if (result != null) {
                    logger.info("✅ Agent [{}] executed. Stored output under key: {}", 
                               task.getAgentId(), outputKey);
                    context.put(outputKey, result);
                } else {
                    logger.info("ℹ️ Agent [{}] executed. Output key '{}' defined, but result was null (this may be intentional)",
                               task.getAgentId(), outputKey);
                }
            } else {
                logger.debug("ℹ️ No outputKey specified for task [{}]. No result stored in context",
                            task.getId());
            }
        } catch (Exception e) {
            logger.error("❌ Failed to execute agent [{}]: {}", task.getAgentId(), e.getMessage());
            throw new RuntimeException("Failed to execute agent: " + task.getAgentId(), e);
        }
    }
}