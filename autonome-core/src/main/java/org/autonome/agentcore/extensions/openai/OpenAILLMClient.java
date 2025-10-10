package org.autonome.agentcore.extensions.openai;

import java.util.List;
import java.util.Map;

import org.autonome.agentcore.LLMClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
@Primary
public class OpenAILLMClient implements LLMClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String callLLM(String prompt, String context, Map<String, Object> config) {
        try {
            String model = (String) config.getOrDefault("model", "gpt-3.5-turbo");
            double temperature = Double.parseDouble(config.getOrDefault("temperature", 0.7).toString());

            String body = objectMapper.writeValueAsString(
                    Map.of(
                            "model", model,
                            "temperature", temperature,
                            "messages", List.of(
                                    Map.of("role", "system", "content", "You are a helpful assistant."),
                                    Map.of("role", "user", "content", prompt))));

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "⚠️ OpenAI call failed: HTTP " + response.code() + " → " + response.message();
                }

                String responseBody = response.body().string();
                JsonNode json = objectMapper.readTree(responseBody);
                return json.path("choices").get(0).path("message").path("content").asText();
            }

        } catch (Exception e) {
            return "⚠️ OpenAI call failed: " + e.getMessage();
        }
    }

}