package org.autonome.agentcore.extensions.python;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.camel.Exchange;
import org.autonome.agentcore.AgentDefinition;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class PythonAgent implements Agent {

    private final AgentDefinition def;

    public PythonAgent(AgentDefinition def) {
        this.def = def;
    }

    @Override
    public String getName() {
        return def.getName() != null ? def.getName() : def.getAgentId();
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String script = def.getConfig().get("scriptPath");
        String input = exchange.getIn().getBody(String.class);

        Process process = new ProcessBuilder("python3", script, input).start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            exchange.getIn().setBody(result.toString());
        }
    }
}
