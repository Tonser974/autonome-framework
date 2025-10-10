package org.autonome.api;

import java.util.Map;

public interface Task {

    String getId();                        // Unique task ID within a flow
    String getName();                      // Human-readable name
    String getDescription();               // Optional for UI/trace/debug
    String getAgentId();                   // ID of the agent responsible
    Map<String, Object> getInput();        // Inputs to the task (could be keys or data)
    String getOutputKey();                 // Key for storing output in context
    boolean isOptional();                  // If task fails, can the flow continue?
    String getFlowRef(); // returns the YAML path to the referenced flow (optional)
    String getCondition(); // Optional condition expression (e.g. env == 'dev')
    String getLoopOver();

}
