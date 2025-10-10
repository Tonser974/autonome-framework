package org.autonome.community.config;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.autonome.agentcore.AgentContextStore;
import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.AgentDefinitionRegistry;
import org.autonome.agentcore.engine.FlowEngineRegistry;
import org.autonome.agentcore.executor.FlowExecutor;
import org.autonome.agentcore.loader.AgentDefinitionLoader;
import org.autonome.agentcore.InMemoryContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;

/**
 * Configuration for Autonome Community Edition.
 * This is a minimal example showing how to configure the framework.
 *
 * <p>For production apps, customize this configuration with:</p>
 * <ul>
 * <li>Custom agent registrations</li>
 * <li>Security configuration</li>
 * <li>Database connection pools</li>
 * <li>Monitoring and observability</li>
 * </ul>
 */
@Configuration
public class AgentCoreConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentCoreConfig.class);
    
    /**
     * HTTP client for making external API calls
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();
    }
    
    
    
    /**
     * Loads agent definitions from YAML and registers them.
     * The YAML file path is configured via ${autonome.agents.path} property.
     */
    @Bean
    public List<AgentDefinition> agentDefinitions(
            AgentDefinitionRegistry registry,
            AgentDefinitionLoader loader) throws IOException {
        try {
            List<AgentDefinition> defs = loader.load();
            logger.info("📄 Loaded {} AgentDefinitions from YAML", defs.size());
            defs.forEach(def -> logger.debug(" 🧠 {}", def.getAgentId()));
            registry.registerAll(defs);
            return defs;
        } catch (IOException e) {
            logger.error("❌ Failed to load AgentDefinitions: {}", e.getMessage());
            throw new IllegalStateException("Could not initialize AgentDefinitions", e);
        }
    }
    
    /**
     * Flow executor orchestrates multi-agent workflows.
     */
    @Bean
    public FlowExecutor flowExecutor(FlowEngineRegistry engineRegistry) {
        return new FlowExecutor(engineRegistry);
    }
    
    /**
     * Agent context store for managing conversation state.
     * Defaults to in-memory storage for demo purposes.
     * Override with custom implementation by setting autonome.context.store property.
     */
    @Bean
    @Primary
    public AgentContextStore contextStore(
            @Value("${autonome.context.store:in-memory}") String storeType) {
        
        if ("in-memory".equalsIgnoreCase(storeType)) {
            logger.info("📦 Initializing InMemoryContextStore");
            return new InMemoryContextStore();
        }
        
        // Default fallback
        logger.warn("⚠️ Unknown context store type '{}', falling back to in-memory", storeType);
        return new InMemoryContextStore();
    }
    
    /**
     * JSON object mapper for serialization.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}