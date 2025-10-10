package org.autonome.agentcore.builtin.fileops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Builds JSON chunks with embeddings and metadata for vector database upsert.
 * Combines text chunks, embedding vectors, and optional YAML metadata.
 * 
 * <p><b>Inputs (Exchange headers):</b></p>
 * <ul>
 *   <li><b>doc_chunks</b> - List&lt;String&gt; of text chunks</li>
 *   <li><b>embedded_chunks</b> - List&lt;Map&gt; with 'vector' key containing embeddings</li>
 *   <li><b>doc_id</b> - String document identifier (file path)</li>
 *   <li><b>metadata_file</b> - (Optional) YAML file with per-document metadata</li>
 * </ul>
 * 
 * <p><b>Output:</b> List of maps ready for vector DB upsert, each containing:</p>
 * <ul>
 *   <li><b>doc_id</b> - Document filename</li>
 *   <li><b>chunk_text</b> - Text content</li>
 *   <li><b>embedding</b> - Vector embedding</li>
 *   <li><b>metadata</b> - Base + custom metadata</li>
 * </ul>
 * 
 * <p><b>Usage in flow:</b></p>
 * <pre>
 * - id: build-vectors
 *   agentId: json-builder-agent
 *   input:
 *     doc_chunks: "@chunks"
 *     embedded_chunks: "@embeddings"
 *     doc_id: "@file_path"
 *     metadata_file: "/config/metadata.yaml"
 *   outputKey: chunks_to_upsert
 * </pre>
 */
@Component
public class JsonBuilderAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonBuilderAgent.class);
    
    private Map<String, Map<String, Object>> metadataMap = new HashMap<>();

    @Override
    public String getName() {
        return "json-builder-agent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) {
        List<String> docChunks = (List<String>) exchange.getIn().getHeader("doc_chunks");
        List<Map<String, Object>> embeddedChunks = (List<Map<String, Object>>) exchange.getIn().getHeader("embedded_chunks");
        String metadataFile = (String) exchange.getIn().getHeader("metadata_file");
        Object docIdObj = exchange.getIn().getHeader("doc_id");

        if (docChunks == null || embeddedChunks == null || docIdObj == null) {
            throw new IllegalArgumentException("Missing one of: doc_chunks, embedded_chunks, or doc_id");
        }

        logger.info("🔨 Building JSON chunks for document: {}", docIdObj);

        // Load metadata if provided
        if (metadataFile != null && metadataMap.isEmpty()) {
            loadMetadata(new File(metadataFile));
        }

        String fullPath = docIdObj.toString();
        String fileName = Paths.get(fullPath).getFileName().toString();

        // Base metadata
        Map<String, Object> baseMetadata = new HashMap<>();
        baseMetadata.put("source", "autonome-docs");
        baseMetadata.put("doc_id", fileName);
        baseMetadata.put("file_path", fullPath);
        baseMetadata.put("doc_type", "txt");
        baseMetadata.put("version", "v1.0.0");

        // Merge dynamic metadata if available
        if (metadataMap.containsKey(fileName)) {
            baseMetadata.putAll(metadataMap.get(fileName));
            logger.debug("   Applied custom metadata for: {}", fileName);
        }

        // Build chunks
        List<Map<String, Object>> upserts = new ArrayList<>();
        for (int i = 0; i < docChunks.size(); i++) {
            Map<String, Object> chunkMap = new HashMap<>();
            chunkMap.put("doc_id", fileName);
            chunkMap.put("chunk_text", docChunks.get(i));
            chunkMap.put("embedding", embeddedChunks.get(i).get("vector"));
            chunkMap.put("metadata", baseMetadata);
            upserts.add(chunkMap);
        }

        context.put("chunks_to_upsert", upserts);
        exchange.getMessage().setBody(upserts);
        
        logger.info("✅ Built {} chunks ready for upsert", upserts.size());
    }

    private void loadMetadata(File yamlFile) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            metadataMap = mapper.readValue(yamlFile, new TypeReference<Map<String, Map<String, Object>>>() {});
            logger.info("✅ Loaded metadata from: {}", yamlFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("❌ Failed to read metadata YAML: {}", yamlFile.getName());
            throw new RuntimeException("Failed to read metadata YAML: " + yamlFile.getName(), e);
        }
    }
}