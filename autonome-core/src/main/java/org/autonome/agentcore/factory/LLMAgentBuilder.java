package org.autonome.agentcore.factory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.AgentExtension;
import org.autonome.agentcore.AgentExtensionRegistry;
import org.autonome.agentcore.ExtensibleLLMAgent;
import org.autonome.agentcore.LLMClient;
import org.autonome.api.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LLMAgentBuilder implements AgentBuilder {

    @Autowired
    private LLMClient llmClient;

    @Autowired
    private AgentExtensionRegistry extensionRegistry;

    @Override
    public boolean supports(String type) {
        return "LLM".equalsIgnoreCase(type);
    }

    @Override
    public Agent build(AgentDefinition def) {
        List<AgentExtension> extensions = def.getEnabledExtensions().stream()
                .map(extensionRegistry::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new ExtensibleLLMAgent(def, extensions, llmClient);
    }
}
