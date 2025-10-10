package org.autonome.agentcore.builtin.fileops;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;

public class LogAgent implements Agent {

    @Override
    public String getName() {
        return "LogAgent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String filePath = (String) exchange.getIn().getHeader("file_path");
        String logPath = (String) exchange.getIn().getHeader("log_path");

        if (filePath == null || logPath == null) {
            throw new IllegalArgumentException("file_path or log_path header missing");
        }

        appendLine(logPath, filePath);
    }

    private void appendLine(String filePath, String line) throws IOException {
        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(line + System.lineSeparator());
        }
    }
}
