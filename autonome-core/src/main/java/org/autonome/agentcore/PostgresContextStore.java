package org.autonome.agentcore;

import java.io.IOException;

import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PostgreSQL-backed context store for production persistence.
 * 
 * <p><b>Enable with:</b></p>
 * <pre>
 * autonome:
 *   context:
 *     store: postgres
 * 
 * spring:
 *   datasource:
 *     url: jdbc:postgresql://localhost:5432/autonome
 *     username: ${DB_USER}
 *     password: ${DB_PASSWORD}
 * </pre>
 * 
 * <p><b>Required Database Schema:</b></p>
 * <pre>
 * CREATE TABLE agent_contexts (
 *   tenant_id VARCHAR(255) NOT NULL,
 *   conversation_id VARCHAR(255) NOT NULL,
 *   context_json JSONB NOT NULL,
 *   updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
 *   PRIMARY KEY (tenant_id, conversation_id)
 * );
 * 
 * CREATE INDEX idx_agent_contexts_tenant ON agent_contexts(tenant_id);
 * CREATE INDEX idx_agent_contexts_updated ON agent_contexts(updated_at);
 * </pre>
 * 
 * <p><b>Characteristics:</b></p>
 * <ul>
 *   <li>Persistent storage with ACID guarantees</li>
 *   <li>Multi-instance safe</li>
 *   <li>JSONB for efficient querying</li>
 *   <li>Automatic serialization/deserialization</li>
 * </ul>
 */
@Component
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnProperty(name = "autonome.context.store", havingValue = "postgres")
public class PostgresContextStore implements AgentContextStore {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgresContextStore.class);
    
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public PostgresContextStore(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        logger.info("✅ PostgresContextStore enabled (production mode)");
    }

    @Override
    public AgentContext getOrCreate(String tenantId, String conversationId) {
        try {
            String json = jdbc.queryForObject(
                "SELECT context_json FROM agent_contexts WHERE tenant_id = ? AND conversation_id = ?",
                String.class, tenantId, conversationId
            );
            AgentContext context = mapper.readValue(json, AgentContext.class);
            logger.debug("Retrieved context for {}:{}", tenantId, conversationId);
            return context;
        } catch (EmptyResultDataAccessException e) {
            logger.debug("Creating new context for {}:{}", tenantId, conversationId);
            return new AgentContext(tenantId, conversationId);
        } catch (IOException e) {
            logger.error("Failed to parse AgentContext for {}:{} - {}", tenantId, conversationId, e.getMessage());
            throw new RuntimeException("Failed to parse AgentContext", e);
        }
    }

    @Override
    public void save(String tenantId, String conversationId, AgentContext context) {
        try {
            String json = mapper.writeValueAsString(context);
            jdbc.update("""
                INSERT INTO agent_contexts (tenant_id, conversation_id, context_json, updated_at)
                VALUES (?, ?, ?::jsonb, now())
                ON CONFLICT (tenant_id, conversation_id)
                DO UPDATE SET context_json = excluded.context_json, updated_at = now()
                """, tenantId, conversationId, json);
            logger.debug("Saved context for {}:{}", tenantId, conversationId);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AgentContext for {}:{} - {}", tenantId, conversationId, e.getMessage());
            throw new RuntimeException("Failed to serialize AgentContext", e);
        }
    }

    @Override
    public void delete(String tenantId, String conversationId) {
        int deleted = jdbc.update(
            "DELETE FROM agent_contexts WHERE tenant_id = ? AND conversation_id = ?",
            tenantId, conversationId
        );
        logger.debug("Deleted {} context(s) for {}:{}", deleted, tenantId, conversationId);
    }

    @Override
    public void clearTenant(String tenantId) {
        int deleted = jdbc.update("DELETE FROM agent_contexts WHERE tenant_id = ?", tenantId);
        logger.info("Cleared {} contexts for tenant: {}", deleted, tenantId);
    }

    @Override
    public void saveAsync(String tenantId, String conversationId, AgentContext context) {
        save(tenantId, conversationId, context);
    }
}