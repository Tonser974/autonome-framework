package org.autonome.agentcore.extensions.rest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.agentcore.AgentDefinition;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class RestAgent implements Agent {

    private final AgentDefinition def;
    private final HttpClient client = HttpClient.newHttpClient();

    public RestAgent(AgentDefinition def) {
        this.def = def;
    }

    @Override
    public String getName() {
        return def.getName() != null ? def.getName() : def.getAgentId();
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        Map<String, String> config = def.getConfig();
        String endpoint = config.get("endpoint");
        String method = config.getOrDefault("method", "POST");

        String body = exchange.getIn().getBody(String.class);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json");

        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.GET();
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        exchange.getIn().setBody(response.body());
    }
}