// FlowEngineRegistry.java
package org.autonome.agentcore.engine;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlowEngineRegistry {
    private final Map<String, FlowEngine> engineMap = new HashMap<>();

    public void register(FlowEngine engine) {
        engineMap.put(engine.getType(), engine);
    }

    public FlowEngine getEngine(String type) {
        if (!engineMap.containsKey(type)) {
            throw new RuntimeException("FlowEngine not found for type: " + type);
        }
        return engineMap.get(type);
    }
}

