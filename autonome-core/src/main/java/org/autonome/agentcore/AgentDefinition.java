package org.autonome.agentcore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDefinition {
    private String agentId;
    private String name;
    private String systemPrompt;
    private String type;
    private List<String> enabledExtensions;
    private boolean humanInLoopEnabled;
    private Map<String, String> config;

    public List<String> getEnabledExtensions() {
        return enabledExtensions != null ? enabledExtensions : Collections.emptyList();
    }

    public Map<String, String> getConfig() {
        return config != null ? config : Collections.emptyMap();
    }

    public String getSystemPrompt() {
        return systemPrompt != null ? systemPrompt : "";
    }

}