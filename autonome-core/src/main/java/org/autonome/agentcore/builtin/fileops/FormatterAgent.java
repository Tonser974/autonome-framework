package org.autonome.agentcore.builtin.fileops;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.springframework.stereotype.Component;

@Component
public class FormatterAgent implements Agent {

    @Override
    public String getName() {
        return "formatter-agent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) {
        List<Map<String, Object>> matches = (List<Map<String, Object>>) exchange.getIn().getHeader("matches");
        String question = (String) exchange.getIn().getHeader("question");

        if (matches == null || matches.isEmpty()) {
            throw new IllegalArgumentException("No matches found to format.");
        }

        String formatted = matches.stream()
            .map(m -> "- " + m.get("chunk_text"))
            .collect(Collectors.joining("\n"));

            String fullPrompt = "Answer the following question using the provided context.\n\n"
            + "Context:\n" + formatted + "\n\n"
            + "Question: " + question;

exchange.getMessage().setBody(fullPrompt);
//context.put("retrieved_context", fullPrompt);
    }
}
