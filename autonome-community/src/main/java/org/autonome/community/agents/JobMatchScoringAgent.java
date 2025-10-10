package org.autonome.community.agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.AgentExtension;
import org.autonome.agentcore.AgentInitializer;
import org.autonome.agentcore.LLMClient;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI-powered job match scoring agent
 * Analyzes how well a candidate fits a job posting
 */
@Component
public class JobMatchScoringAgent implements Agent, AgentInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JobMatchScoringAgent.class);
    
    @Autowired
    @Qualifier("claudeLLMClient")
    private LLMClient llmClient;
    
    private final ObjectMapper objectMapper;
    
    private AgentDefinition definition;
    private List<AgentExtension> extensions;
    
    public JobMatchScoringAgent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void init(AgentDefinition definition, List<AgentExtension> extensions) {
        this.definition = definition;
        this.extensions = extensions;
    }
    
    @Override
    public String getName() {
        return definition != null ? definition.getName() : "job-match-scoring-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        // Extract input data
        @SuppressWarnings("unchecked")
        Map<String, Object> jobData = (Map<String, Object>) exchange.getIn().getHeader("job_data");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> candidateData = (Map<String, Object>) exchange.getIn().getHeader("candidate_data");
        
        if (jobData == null || candidateData == null) {
            throw new IllegalArgumentException("job_data and candidate_data are required");
        }
        
        logger.info("Analyzing match for: {} at {}", 
            candidateData.get("name"), jobData.get("title"));
        
        try {
            // Build analysis prompt
            String prompt = buildMatchPrompt(jobData, candidateData);
            
            // Configure LLM call - FIXED: Use proper type-safe get methods
            Map<String, Object> config = new HashMap<>();
            config.put("systemPrompt", getSystemPrompt());
            config.put("model", context.get("model", String.class) != null ? context.get("model", String.class) : "claude-3-5-sonnet-20241022");
            config.put("temperature", context.get("temperature", Double.class) != null ? context.get("temperature", Double.class) : 0.3);
            config.put("max_tokens", context.get("max_tokens", Integer.class) != null ? context.get("max_tokens", Integer.class) : 2000);
            
            // Call LLM for analysis
            String llmResponse = llmClient.callLLM(prompt, null, config);
            
            // Parse and return result
            Map<String, Object> matchResult = parseMatchResult(llmResponse);
            matchResult.put("analyzed_at", java.time.Instant.now().toString());
            
            logger.info("Match score: {}/100", matchResult.get("total_score"));
            
            exchange.getMessage().setBody(matchResult);
            
        } catch (Exception e) {
            logger.error("Error scoring match: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("total_score", 0);
            errorResponse.put("error", e.getMessage());
            exchange.getMessage().setBody(errorResponse);
        }
    }
    
    private String buildMatchPrompt(Map<String, Object> job, Map<String, Object> candidate) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== JOB POSTING ===\n");
        prompt.append("Title: ").append(job.getOrDefault("title", "N/A")).append("\n");
        prompt.append("Company: ").append(job.getOrDefault("company", "N/A")).append("\n");
        prompt.append("Required Skills: ").append(job.getOrDefault("required_skills", "N/A")).append("\n");
        prompt.append("Description: ").append(job.getOrDefault("description", "N/A")).append("\n\n");
        
        prompt.append("=== CANDIDATE ===\n");
        prompt.append("Name: ").append(candidate.getOrDefault("name", "N/A")).append("\n");
        prompt.append("Experience: ").append(candidate.getOrDefault("years_experience", "N/A")).append(" years\n");
        prompt.append("Skills: ").append(candidate.getOrDefault("skills", "N/A")).append("\n");
        prompt.append("Background: ").append(candidate.getOrDefault("background", "N/A")).append("\n\n");
        
        prompt.append("Analyze this match and return ONLY a JSON object with this structure:\n");
        prompt.append("{\n");
        prompt.append("  \"total_score\": <0-100>,\n");
        prompt.append("  \"strengths\": [\"...\"],\n");
        prompt.append("  \"gaps\": [\"...\"],\n");
        prompt.append("  \"recommendation\": \"hire|interview|pass\",\n");
        prompt.append("  \"summary\": \"...\"\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    @SuppressWarnings("unchecked")
private Map<String, Object> parseMatchResult(String llmResponse) {
    try {
        // LOG THE RAW RESPONSE
        logger.info("Raw LLM Response: {}", llmResponse);
        
        // Clean markdown code blocks if present
        String cleaned = llmResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        logger.info("Cleaned response: {}", cleaned.trim());
        
        return objectMapper.readValue(cleaned.trim(), Map.class);
        
    } catch (Exception e) {
        logger.error("Failed to parse LLM response: {}", e.getMessage());
        logger.error("Raw response was: {}", llmResponse);
        
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("total_score", 0);
        fallback.put("error", "Failed to parse scoring result");
        fallback.put("raw_response", llmResponse);  // Include raw response in error
        return fallback;
    }
}
    
    private String getSystemPrompt() {
        if (definition != null && definition.getSystemPrompt() != null) {
            return definition.getSystemPrompt();
        }
        
        return """
            You are an expert recruiter analyzing candidate-job matches.
            
            Evaluate the match on a 0-100 scale based on:
            - Skills alignment (40 points)
            - Experience relevance (30 points)
            - Cultural fit indicators (20 points)
            - Growth potential (10 points)
            
            Provide specific, actionable feedback.
            Return ONLY valid JSON with no additional text.
            """;
    }
}