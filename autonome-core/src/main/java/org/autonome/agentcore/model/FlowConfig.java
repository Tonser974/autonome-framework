package org.autonome.agentcore.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class FlowConfig {
    private String id;
    private String name;
    private String type;
    private String description;
    private Map<String, Object> globals;
    private List<TaskDefinition> taskDefinitions;
}