// InputResolver.java
// InputResolver.java
package org.autonome.agentcore.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.autonome.context.AgentContext;

public class InputResolver {

    public static void resolveIntoExchange(Exchange exchange, Map<String, Object> inputs, AgentContext context) {
        Message message = exchange.getIn();

        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            String key = entry.getKey();
            Object value = resolveValue(entry.getValue(), context);
            message.setHeader(key, value);
        }
    }

    private static Object resolveValue(Object value, AgentContext context) {
        if (value instanceof String str && str.startsWith("${") && str.endsWith("}")) {
            String ref = str.substring(2, str.length() - 1);
            return context.getData().getOrDefault(ref, str);
        } else if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> resolveValue(item, context))
                    .toList();
        } else if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> resolveValue(e.getValue(), context)));
        } else {
            return value;
        }
    }

    private static Object resolveReference(String ref, AgentContext context) {
        String[] parts = ref.split("\\.");
        Object current = context.get(parts[0]);

        if (current == null) {
            throw new RuntimeException("Reference not found in context: " + parts[0]);
        }

        for (int i = 1; i < parts.length; i++) {
            if (current instanceof Map<?, ?>) {
                current = ((Map<?, ?>) current).get(parts[i]);
                if (current == null) {
                    throw new RuntimeException("Reference part not found: " + parts[i]);
                }
            } else {
                throw new RuntimeException("Cannot resolve reference part: " + parts[i]);
            }
        }
        return current;
    }

}
