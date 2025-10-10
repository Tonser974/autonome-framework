package org.autonome.api;

import org.apache.camel.Exchange;
import org.autonome.context.AgentContext;

public interface Agent {
    String getName();
    void handle(Exchange exchange, AgentContext context) throws Exception;
}