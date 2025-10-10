package org.autonome.agentcore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class ExtensibleLLMAgent implements Agent {

    private final AgentDefinition definition;
    private final List<AgentExtension> extensions;
    private final LLMClient llmClient;

    public ExtensibleLLMAgent(AgentDefinition definition,
                              List<AgentExtension> extensions,
                              LLMClient llmClient) {
        this.definition = definition;
        this.extensions = extensions;
        this.llmClient = llmClient;
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public void handle(org.apache.camel.Exchange exchange, AgentContext context) throws Exception {
        String userMessage = (String) exchange.getIn().getHeader("user_input");
        if (userMessage == null) {
            throw new IllegalArgumentException("Missing required input: user_input");
        }
        
        context.addMessage("User: " + userMessage);

        // Collect tool responses
        Map<String, Object> toolResults = new HashMap<>();
        for (AgentExtension ext : extensions) {
            Object result = ext.execute(userMessage, context);
            toolResults.put(ext.getName(), result);
        }

        // Build prompt for LLM
        String prompt = definition.getSystemPrompt() + "\n\n"
                      + "Conversation so far:\n"
                      + String.join("\n", context.getConversationLog()) + "\n\n"
                      + "Tools:\n" + toolResults + "\n\n"
                      + "User: " + userMessage;

                      Map<String, Object> config = new HashMap<>(definition.getConfig());
                config.putAll(context.getData()); // flow-level overrides
                String llmResponse = llmClient.callLLM(prompt, null, config);

        //String llmResponse = llmClient.callLLM(prompt, null);  // `null` for context override, if needed
        context.addMessage("Agent: " + llmResponse);

        exchange.getIn().setBody(llmResponse);
    }
}