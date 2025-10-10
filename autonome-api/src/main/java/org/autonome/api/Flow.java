package org.autonome.api;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Flow {
    private String id;
    private String name;
    private String type;
    private String description;
    private List<Task> tasks;
    private Map<String, Object> globals;
}
