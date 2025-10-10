package org.autonome.community.agents;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.autonome.agentcore.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Claude LLM Client for Anthropic API integration
 * Handles communication with Claude AI models
 */
@Component
@Qualifier("claudeLLMClient")
public class ClaudeLLMClient implements LLMClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ClaudeLLMClient.class);
    
    @Value("${anthropic.api.key:}")
    private String apiKey;
    
    @Value("${claude.api.key:}")
    private String claudeApiKey;
    
    @Value("${ANTHROPIC_API_KEY:}")
    private String envApiKey;
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .callTimeout(240, TimeUnit.SECONDS)
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String callLLM(String prompt, String context, Map<String, Object> config) {
        try {
            String actualApiKey = getApiKey();
            
            if (actualApiKey == null || actualApiKey.isEmpty()) {
                logger.error("No Claude API key found");
                return "⚠️ Anthropic API key not configured. Please set anthropic.api.key in application.properties or ANTHROPIC_API_KEY environment variable.";
            }

            // Extract config parameters
            String model = (String) config.getOrDefault("model", "claude-3-5-sonnet-20241022");
            int maxTokens = Integer.parseInt(config.getOrDefault("max_tokens", 2000).toString());
            double temperature = Double.parseDouble(config.getOrDefault("temperature", 0.3).toString());
            String systemPrompt = (String) config.getOrDefault("systemPrompt", "You are a helpful AI assistant.");

            logger.debug("Calling Claude API - Model: {}, Temp: {}, MaxTokens: {}", model, temperature, maxTokens);

            // Build request body
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "temperature", temperature,
                "system", systemPrompt,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                )
            );

            String body = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", actualApiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("User-Agent", "Autonome/1.0")
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    logger.error("Claude API error: {} - {}", response.code(), errorBody);
                    
                    return switch (response.code()) {
                        case 401 -> "⚠️ Claude API authentication failed. Please verify your API key.";
                        case 429 -> "⚠️ Claude API rate limit exceeded - please try again later";
                        case 500, 502, 503 -> "⚠️ Claude service temporarily unavailable - please try again";
                        default -> "⚠️ Claude API error: HTTP " + response.code();
                    };
                }

                String responseBody = response.body().string();
                JsonNode json = objectMapper.readTree(responseBody);
                
                // Parse Claude response: { "content": [{"text": "..."}] }
                if (!json.has("content") || json.path("content").size() == 0) {
                    logger.error("Unexpected Claude response structure");
                    return "⚠️ Claude returned unexpected response format";
                }
                
                JsonNode contentArray = json.path("content");
                String content = contentArray.get(0).path("text").asText();
                
                if (content == null || content.trim().isEmpty()) {
                    return "⚠️ Claude returned empty response";
                }
                
                logger.debug("Claude response received successfully ({} characters)", content.length());
                return content;
            }
            
        } catch (java.net.SocketTimeoutException e) {
            logger.error("Claude API timeout: {}", e.getMessage());
            return "⚠️ Claude call timed out - request took too long";
        } catch (java.io.IOException e) {
            logger.error("Claude API IO error: {}", e.getMessage());
            return "⚠️ Claude call failed: network error - " + e.getMessage();
        } catch (Exception e) {
            logger.error("Claude API unexpected error", e);
            return "⚠️ Claude call failed: " + e.getMessage();
        }
    }
    
    /**
     * Get API key from multiple possible sources
     */
    private String getApiKey() {
        if (isValidKey(apiKey)) return apiKey;
        if (isValidKey(claudeApiKey)) return claudeApiKey;
        if (isValidKey(envApiKey)) return envApiKey;
        
        String systemEnvKey = System.getenv("ANTHROPIC_API_KEY");
        if (isValidKey(systemEnvKey)) return systemEnvKey;
        
        return null;
    }
    
    /**
     * Check if a key is valid (not null, empty, or placeholder)
     */
    private boolean isValidKey(String key) {
        return key != null 
            && !key.isEmpty() 
            && !key.startsWith("${");
    }
}