package org.autonome.agentcore.loader;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.autonome.api.Flow;
import org.autonome.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = { FlowLoader.class })
@TestPropertySource(properties = {
    "autonome.flows.dir=target/test-classes/flows"
})
class FlowLoaderTest {

@Autowired
private FlowLoader flowLoader;

@Test
void testSimpleFlowWithInputsAndOutputs() throws Exception {
    Flow flow = flowLoader.loadFromYaml("flow-with-inputs.yaml");

    assertEquals("demo-flow", flow.getId());
    assertEquals("My Flow", flow.getName());
    assertEquals("sequential", flow.getType());
    assertEquals("Demo flow for testing", flow.getDescription());

    Map<String, Object> globals = flow.getGlobals();
    assertNotNull(globals);
    assertEquals("value1", globals.get("key1"));

    List<Task> tasks = flow.getTasks();
    assertEquals(2, tasks.size());

    Task task1 = tasks.get(0);
    assertEquals("task1", task1.getId());
    assertEquals("echo-agent", task1.getAgentId());
    assertEquals("Hello!", task1.getInput().get("message"));
    assertEquals("result1", task1.getOutputKey());
    assertFalse(task1.isOptional());

    Task task2 = tasks.get(1);
    assertTrue(task2.isOptional());
}

@Test
void testAutonomeDocEmbedFlow() throws Exception {
    Flow flow = flowLoader.loadFromYaml("embed-autonome-doc.yaml");

    assertEquals("autonome-doc-embed", flow.getId());
    assertEquals("Embed Autonome Docs", flow.getName());
    assertEquals("sequential", flow.getType());
    assertEquals("Embed Autonome documentation content into Supabase for later search", flow.getDescription());

    Map<String, Object> globals = flow.getGlobals();
    assertTrue(globals.containsKey("doc_text"));
    assertTrue(globals.get("doc_text").toString().startsWith("Autonome is a framework"));

    List<Task> tasks = flow.getTasks();
    assertEquals(2, tasks.size());

    Task embedTask = tasks.get(0);
    assertEquals("embedDoc", embedTask.getId());
    assertEquals("openAIEmbedAgent", embedTask.getAgentId());
    assertEquals("@doc_text", embedTask.getInput().get("user_input"));
    assertEquals("embedding", embedTask.getOutputKey());
    assertFalse(embedTask.isOptional());

    Task insertTask = tasks.get(1);
    assertEquals("insertDoc", insertTask.getId());
    assertEquals("supabaseInsertAgent", insertTask.getAgentId());
    assertEquals("@doc_text", insertTask.getInput().get("text"));
    assertEquals("@embedding", insertTask.getInput().get("embedding"));
    assertEquals("insert_status", insertTask.getOutputKey());
}

}

