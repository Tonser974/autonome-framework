package org.autonome.agentcore.examples;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.springframework.stereotype.Component;

@Component("summarizerAgent")
public class SummarizerAgent implements Agent {
    @Override
    public String getName() {
        return "Summarizer Agent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) {
        String input = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody("Summary: " + input.substring(0, Math.min(30, input.length())) + "...");
    }
}