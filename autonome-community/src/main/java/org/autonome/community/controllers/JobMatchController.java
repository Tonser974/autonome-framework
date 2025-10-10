package org.autonome.community.controllers;

import java.util.HashMap;
import java.util.Map;

import org.autonome.agentcore.loader.FlowLoader;
import org.autonome.api.Flow;
import org.autonome.context.AgentContext;
import org.autonome.runtime.FlowRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for job match scoring
 * 
 * Example usage:
 * POST /api/demo/job-match
 * {
 *   "job_data": {
 *     "title": "Senior Java Developer",
 *     "company": "Tech Corp",
 *     "required_skills": "Java, Spring Boot, REST APIs",
 *     "description": "Build scalable microservices..."
 *   },
 *   "candidate_data": {
 *     "name": "Jane Smith",
 *     "years_experience": 8,
 *     "skills": "Java, Spring, Kubernetes, AWS",
 *     "background": "Built payment processing systems at FinTech startup..."
 *   }
 * }
 */
@RestController
@RequestMapping("/api/demo/job-match")
@CrossOrigin(origins = "*")
public class JobMatchController {

    private static final Logger logger = LoggerFactory.getLogger(JobMatchController.class);

    @Autowired
    private FlowRuntime flowRuntime;
    
    @Autowired
    private FlowLoader flowLoader;

    @PostMapping
    public ResponseEntity<Map<String, Object>> scoreMatch(@RequestBody Map<String, Object> request) {
        logger.info("Received job match scoring request");
        
        try {
            // Extract input
            @SuppressWarnings("unchecked")
            Map<String, Object> jobData = (Map<String, Object>) request.get("job_data");
            @SuppressWarnings("unchecked")
            Map<String, Object> candidateData = (Map<String, Object>) request.get("candidate_data");
            
            if (jobData == null || candidateData == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "job_data and candidate_data are required"));
            }
            
            // Prepare context
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("job_data", jobData);
            contextData.put("candidate_data", candidateData);
            
            // Load and run flow
            Flow matchFlow = flowLoader.loadFromYaml("job-match-scoring.flow.yaml");
            if (matchFlow == null) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Flow not found: job-match-scoring.flow.yaml"));
            }
            
            String conversationId = "demo_" + System.currentTimeMillis();
            AgentContext context = flowRuntime.run("demo", conversationId, matchFlow, contextData);
            
            // Extract result
            Object result = context.get("match_result");
            
            if (result == null) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "No match result returned"));
            }
            
            logger.info("Job match scoring completed successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result
            ));
            
        } catch (Exception e) {
            logger.error("Error in job match scoring", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", e.getMessage(),
                    "success", false
                ));
        }
    }
}