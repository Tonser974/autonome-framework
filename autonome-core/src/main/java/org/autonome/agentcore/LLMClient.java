package org.autonome.agentcore;

import java.util.Map;

public interface LLMClient {
    String callLLM(String prompt, String context, Map<String, Object> config);
}

