package org.autonome.agentcore.model;

import java.util.Map;

import org.autonome.api.Task;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleTask implements Task {
    private final String id;
    private final String name;
    private final String description;
    private final String agentId;
    private final Map<String, Object> input;
    private final String outputKey;
    private final boolean optional;
    private final String flowRef;
    private final String condition;
    private final String loopOver;
}


