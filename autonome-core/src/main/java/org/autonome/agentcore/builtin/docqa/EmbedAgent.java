
package org.autonome.agentcore.builtin.docqa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class EmbedAgent implements Agent {

    @Override
    public String getName() {
        return "embedAgent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        List<String> chunks = (List<String>) exchange.getIn().getHeader("chunks");


        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks found for embedding.");
        }

        List<Map<String, Object>> embeddedChunks = new ArrayList<>();
        for (String chunk : chunks) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("chunk", chunk);
            entry.put("vector", generateDummyEmbedding(chunk)); // 👈 placeholder
            embeddedChunks.add(entry);
        }

        //context.put("embedded_chunks", embeddedChunks);
        exchange.getMessage().setBody(embeddedChunks);

    }

    // 🔄 Dummy embedding function — replace with real vector service later
    private List<Double> generateDummyEmbedding(String chunk) {
        List<Double> vec = new ArrayList<>();
        for (int i = 0; i < 1536; i++) {
            vec.add(Math.random());
        }
        return vec;
    }
    
}
