package org.autonome.agentcore.examples;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.springframework.stereotype.Component;

@Component("greetAgent")
public class GreetAgent implements Agent {
    @Override
    public String getName() {
        return "Greet Agent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) {
        String input = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody("Hi! I am an agent responding to: " + input);
    }
}