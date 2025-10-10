package org.autonome.agentcore.builtin.fileops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Scans a folder for unprocessed files by comparing against a processed log.
 * 
 * <p><b>Inputs (Exchange headers):</b></p>
 * <ul>
 *   <li><b>folder_path</b> - String path to folder to scan</li>
 *   <li><b>processed_log</b> - String path to log file containing processed file paths</li>
 * </ul>
 * 
 * <p><b>Output:</b> List&lt;String&gt; of absolute paths to unprocessed .md and .txt files</p>
 * 
 * <p><b>Usage in flow:</b></p>
 * <pre>
 * - id: scan-docs
 *   agentId: ScanFolderAgent
 *   input:
 *     folder_path: "/data/docs"
 *     processed_log: "/data/processed.txt"
 *   outputKey: unprocessed_files
 * </pre>
 */
@Component
public class ScanFolderAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanFolderAgent.class);

    @Override
    public String getName() {
        return "ScanFolderAgent";
    }

    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String folderPath = (String) exchange.getIn().getHeader("folder_path");
        String processedLog = (String) exchange.getIn().getHeader("processed_log");

        if (folderPath == null || processedLog == null) {
            throw new IllegalArgumentException("folder_path or processed_log missing from context");
        }

        logger.info("📂 Scanning folder: {}", folderPath);
        
        File folder = new File(folderPath);
        logger.debug("   Folder exists: {}, Absolute path: {}", folder.exists(), folder.getAbsolutePath());

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".md") || name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            logger.info("ℹ️ No .md or .txt files found in folder");
            exchange.getIn().setBody(Collections.emptyList());
            return;
        }

        // Load processed files log
        Set<String> processed = new HashSet<>();
        File logFile = new File(processedLog);
        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                processed.addAll(reader.lines().collect(Collectors.toSet()));
            }
            logger.debug("   Loaded {} processed files from log", processed.size());
        }

        // Filter unprocessed files
        List<String> unprocessed = Arrays.stream(files)
                .map(File::getAbsolutePath)
                .filter(f -> !processed.contains(f) && !f.endsWith("processed.txt"))
                .collect(Collectors.toList());

        exchange.getIn().setBody(unprocessed);
        
        logger.info("✅ Found {} unprocessed file(s)", unprocessed.size());
        unprocessed.forEach(file -> logger.debug("   📄 {}", file));
    }
}