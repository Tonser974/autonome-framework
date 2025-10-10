package org.autonome.agentcore.builtin.docqa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;

/**
 * Generates embeddings for text chunks using OpenAI's embedding API.
 * 
 * <p><b>Inputs (Exchange headers):</b></p>
 * <ul>
 *   <li><b>chunks</b> - List&lt;String&gt; of text chunks to embed</li>
 *   <li><b>model</b> - String embedding model name (e.g., "text-embedding-ada-002")</li>
 * </ul>
 * 
 * <p><b>Output:</b> List of maps containing:</p>
 * <ul>
 *   <li><b>chunk</b> - Original text chunk</li>
 *   <li><b>vector</b> - List&lt;Double&gt; embedding vector</li>
 * </ul>
 * 
 * <p><b>Configuration:</b> Requires ${openai.api.key} property</p>
 */
@Component
public class OpenAIEmbeddingAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIEmbeddingAgent.class);
    
    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Override
    public String getName() {
        return "openai-embedding-agent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        List<String> chunks = (List<String>) exchange.getIn().getHeader("chunks");
        String model = (String) exchange.getIn().getHeader("model");

        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'chunks' input");
        }
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("Missing 'model' input");
        }

        logger.info("🔤 Generating embeddings for {} chunks using model: {}", chunks.size(), model);
        logger.debug("Chunk preview: {}", chunks.size() > 0 ? chunks.get(0).substring(0, Math.min(100, chunks.get(0).length())) : "");

        OpenAiService service = new OpenAiService(openAiApiKey);
        List<Map<String, Object>> embeddedChunks = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(model)
                    .input(List.of(chunk))
                    .build();

            EmbeddingResult result = service.createEmbeddings(request);
            List<Double> embedding = result.getData().get(0).getEmbedding();

            Map<String, Object> entry = new HashMap<>();
            entry.put("chunk", chunk);
            entry.put("vector", embedding);
            embeddedChunks.add(entry);
            
            logger.debug("   Embedded chunk {}/{} (vector dimension: {})", i + 1, chunks.size(), embedding.size());
        }

        exchange.getMessage().setBody(embeddedChunks);
        logger.info("✅ Generated {} embeddings", embeddedChunks.size());
    }
}