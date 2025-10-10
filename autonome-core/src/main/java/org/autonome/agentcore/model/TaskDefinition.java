package org.autonome.agentcore.model;

import java.util.Map;

import lombok.Data;

@Data
public class TaskDefinition {
    private String id;
    private String name;
    private String description;
    private String agentId;
    private Map<String, Object> input;    // raw input with @refs
    private String outputKey;
    private boolean optional;
    private String flowRef;
    private String condition;
    private String loopOver; // Optional: for looping over a collection



}