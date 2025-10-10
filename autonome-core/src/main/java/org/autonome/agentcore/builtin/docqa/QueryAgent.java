
// QueryAgent.java
package org.autonome.agentcore.builtin.docqa;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class QueryAgent implements Agent {

    @Override
    public String getName() {
        return "QueryAgent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String userInput = (String) exchange.getIn().getHeader("user_input");
        if (userInput == null) {
            throw new IllegalArgumentException("user_input header missing");
        }
        // In a real case, you would vector search against embeddings
        exchange.getIn().setBody("Pretend answer to: " + userInput);
    }
}

