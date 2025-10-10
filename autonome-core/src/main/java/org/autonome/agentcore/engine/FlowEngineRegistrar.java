// FlowEngineRegistrar.java
package org.autonome.agentcore.engine;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FlowEngineRegistrar {
    public FlowEngineRegistrar(FlowEngineRegistry registry, List<FlowEngine> engines) {
        engines.forEach(registry::register); // ✅ Correct: register(FlowEngine)
    }
}

