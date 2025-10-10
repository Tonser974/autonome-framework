package org.autonome.agentcore.builtin.docqa;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Loads text content from a file.
 * 
 * <p><b>Inputs:</b></p>
 * <ul>
 *   <li><b>file_path</b> - String file path (from Exchange header or AgentContext)</li>
 * </ul>
 * 
 * <p><b>Output:</b> String content of the file</p>
 * 
 * <p><b>Usage in flow:</b></p>
 * <pre>
 * - id: load-doc
 *   agentId: LoadDocAgent
 *   input:
 *     file_path: "/path/to/document.txt"
 *   outputKey: doc_content
 * </pre>
 */
@Component
public class LoadDocAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadDocAgent.class);

    @Override
    public String getName() {
        return "LoadDocAgent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String filePath = (String) exchange.getIn().getHeader("file_path");
        
        if (filePath == null) {
            filePath = (String) context.get("file_path");
        }
        
        if (filePath == null) {
            throw new IllegalArgumentException("file_path is missing in both header and context");
        }

        logger.info("📄 Loading content from file: {}", filePath);
        
        String content = Files.readString(Paths.get(filePath));
        exchange.getIn().setBody(content);
        
        logger.debug("✅ Loaded {} characters from: {}", content.length(), filePath);
    }
}