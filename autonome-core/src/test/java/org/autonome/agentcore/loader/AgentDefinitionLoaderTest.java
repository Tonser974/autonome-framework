package org.autonome.agentcore.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.autonome.agentcore.AgentDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class AgentDefinitionLoaderTest {

    private AgentDefinitionLoader loader;

    private static final String TEST_YAML_PATH = "test-agents.yaml";

    @BeforeEach
    public void setup() throws IOException {
        loader = new AgentDefinitionLoader();

        // Create a temporary test agents.yaml
        String yaml = 
  "- agentId: testAgent\n" +
  "  name: Test Agent\n" +
  "  type: LLM\n" +
  "  systemPrompt: You are a test agent.\n";


        FileWriter writer = new FileWriter(TEST_YAML_PATH);
        writer.write(yaml);
        writer.close();

        // Inject test path
        ReflectionTestUtils.setField(loader, "agentFilePath", TEST_YAML_PATH);
    }

    @Test
    public void testLoad() throws IOException {
        List<AgentDefinition> agents = loader.load();
        assertNotNull(agents);
        assertEquals(1, agents.size());

        AgentDefinition agent = agents.get(0);
        assertEquals("testAgent", agent.getAgentId());
        assertEquals("Test Agent", agent.getName());
        assertEquals("LLM", agent.getType());
    }

    @Test
    public void testFileNotFoundThrows() {
        ReflectionTestUtils.setField(loader, "agentFilePath", "nonexistent.yaml");

        Exception e = assertThrows(IOException.class, () -> loader.load());
        assertTrue(e.getMessage().contains("nonexistent"));
    }
}