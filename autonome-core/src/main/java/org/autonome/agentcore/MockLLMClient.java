package org.autonome.agentcore;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class MockLLMClient implements LLMClient {
    @Override
    public String callLLM(String prompt, String context, Map<String, Object> config) {
        return "🧪 MOCK RESPONSE for prompt: " + prompt;
    }
}

