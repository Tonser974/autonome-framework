// ChunkerAgent.javapackage org.autonome.agentcore.builtin.docqa;
package org.autonome.agentcore.builtin.docqa;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class ChunkerAgent implements Agent {
    @Override
    public String getName() {
        return "chunker-agent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String rawText = (String) exchange.getIn().getHeader("text");
        if (rawText == null || rawText.isEmpty()) {
            throw new IllegalArgumentException("Missing required 'text' header");
        }

        int chunkSize = 300; // default
        Object chunkSizeObj = exchange.getIn().getHeader("chunk_size");
        if (chunkSizeObj != null) {
            try {
                chunkSize = Integer.parseInt(chunkSizeObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        List<String> chunks = splitIntoChunks(rawText, chunkSize);
        exchange.getMessage().setBody(chunks); // ✅ clean return
    }

    private List<String> splitIntoChunks(String text, int size) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }
}