package org.autonome.agentcore.visualizer;


import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskExecutionRecord {
    private String taskId;
    private String taskName;
    private String agentId;
    private Map<String, Object> resolvedInputs;
    private String outputKey;
    private Object result;
    private String status;  // ✅ Success / ⚠️ Optional Failed / ❌ Failed
}
