package org.autonome.agentcore.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.autonome.agentcore.model.FlowConfig;
import org.autonome.agentcore.model.SimpleTask;
import org.autonome.agentcore.model.TaskDefinition;
import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads flow definitions from YAML files.
 * Supports loading from:
 * 1. Absolute paths
 * 2. Relative paths (using configured flows directory)
 * 3. Classpath resources (fallback)
 */
@Component
public class FlowLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowLoader.class);
    
    private final String flowsDir;

    public FlowLoader(@Value("${autonome.flows.dir}") String flowsDir) {
        this.flowsDir = flowsDir.endsWith("/") ? flowsDir : flowsDir + "/";
    }

    public Flow loadFromYaml(String path) throws Exception {
        String resolvedPath;
        
        // First try absolute or directly resolvable relative path
        File direct = new File(path);
        if (direct.isAbsolute() || direct.exists()) {
            resolvedPath = path;
        } else {
            resolvedPath = new File(flowsDir, path).getPath();
        }

        logger.debug("📄 Loading flow file from path: {}", resolvedPath);
        
        File file = new File(resolvedPath);
        if (!file.exists()) {
            // Try classpath as fallback
            logger.debug("⚠️ File not found at path. Trying to load from classpath...");
            URL resource = getClass().getClassLoader().getResource(resolvedPath);
            if (resource == null) {
                throw new FileNotFoundException("Could not find flow file: " + resolvedPath);
            }
            file = new File(resource.toURI());
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        FlowConfig config = mapper.readValue(file, FlowConfig.class);
        
        List<Task> tasks = config.getTaskDefinitions().stream()
                .map(FlowLoader::convertToTask)
                .collect(Collectors.toList());

        Flow flow = new Flow(
            config.getId(), 
            config.getName(), 
            config.getType(), 
            config.getDescription(), 
            tasks,
            config.getGlobals()
        );
        
        logger.info("✅ Loaded flow [{}] with {} tasks", flow.getId(), tasks.size());
        return flow;
    }

    private static Task convertToTask(TaskDefinition def) {
        return SimpleTask.builder()
                .id(def.getId())
                .name(def.getName())
                .description(def.getDescription())
                .agentId(def.getAgentId())
                .input(def.getInput())
                .outputKey(def.getOutputKey())
                .optional(def.isOptional())
                .flowRef(def.getFlowRef())
                .condition(def.getCondition())
                .loopOver(def.getLoopOver())
                .build();
    }
}