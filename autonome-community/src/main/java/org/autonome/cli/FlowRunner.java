package org.autonome.cli;

import org.autonome.agentcore.AgentContextStore;
import org.autonome.agentcore.executor.FlowExecutor;
import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * CLI runner for executing flows from command line
 * Activate with: mvn spring-boot:run -Dspring-boot.run.profiles=cli
 * 
 * Usage:
 *   --flow=<path>              Flow YAML file to execute
 *   --conversationId=<id>      Conversation identifier
 *   --input=<text>             User input text
 */
@Component
@Profile("cli")
public class FlowRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowRunner.class);
    
    private final FlowExecutor flowExecutor;
    private final AgentContextStore contextStore;
    private final FlowLoader flowLoader;
    
    public FlowRunner(
            FlowExecutor flowExecutor,
            AgentContextStore contextStore,
            FlowLoader flowLoader) {
        this.flowExecutor = flowExecutor;
        this.contextStore = contextStore;
        this.flowLoader = flowLoader;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Default values
        String flowPath = "file-analyzer.yaml";
        String tenantId = "cli";
        String conversationId = "cli-" + System.currentTimeMillis();
        String userInput = "Hello from CLI";
        
        // Parse command line arguments
        for (String arg : args) {
            if (arg.startsWith("--flow=")) {
                flowPath = arg.substring("--flow=".length());
            } else if (arg.startsWith("--conversationId=")) {
                conversationId = arg.substring("--conversationId=".length());
            } else if (arg.startsWith("--input=")) {
                userInput = arg.substring("--input=".length());
            }
        }
        
        logger.info("Running flow from: {}", flowPath);
        
        // Load and execute flow
        Flow flow = flowLoader.loadFromYaml(flowPath);
        AgentContext context = contextStore.getOrCreate(tenantId, conversationId);
        context.getData().put("user_input", userInput);
        
        flowExecutor.run(flow, context);
        contextStore.save(tenantId, conversationId, context);
        
        // Output results
        logger.info("Flow completed for conversationId: {}", conversationId);
        
        System.out.println("\n=== Context Data ===");
        context.getData().forEach((key, value) -> 
            System.out.println("  " + key + " = " + (value != null ? value.toString().substring(0, Math.min(100, value.toString().length())) : "null"))
        );
        
        System.out.println("\n=== Conversation Log ===");
        context.getConversationLog().forEach(msg -> 
            System.out.println("  " + msg)
        );
    }
}