package org.autonome.agentcore.loader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.autonome.agentcore.AgentDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads agent definitions from a YAML configuration file.
 * Agent definitions specify how to create and configure agents.
 */
@Component
public class AgentDefinitionLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentDefinitionLoader.class);
    
    @Value("${autonome.agents.path}")
    private String agentFilePath;

    public List<AgentDefinition> load() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File file = new File(agentFilePath);
        
        logger.info("📦 Loading agent definitions from: {}", file.getAbsolutePath());
        
        AgentDefinition[] definitions = mapper.readValue(file, AgentDefinition[].class);
        logger.info("✅ Loaded {} agent definitions", definitions.length);
        
        return Arrays.asList(definitions);
    }
}