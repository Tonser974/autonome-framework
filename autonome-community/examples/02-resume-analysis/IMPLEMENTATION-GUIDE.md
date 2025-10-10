# Resume Analysis Pipeline - Implementation Guide

## Overview

This guide walks you through implementing the Resume Analysis Pipeline from scratch. You'll create three agents that work together to parse, analyze, and summarize resumes.

## What You'll Build

A 3-agent pipeline that:
1. **Parses** resume text into structured data
2. **Extracts** technical skills
3. **Generates** a professional summary

## Architecture

resume_text → ResumeParserAgent → parsed_data
↓
SkillExtractorAgent → skills
↓
SummaryGeneratorAgent → summary

## Step 1: Create ResumeParserAgent

**File:** `src/main/java/org/autonome/community/agents/ResumeParserAgent.java`
```java
package org.autonome.community.agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.agentcore.AgentDefinition;
import org.autonome.agentcore.AgentExtension;
import org.autonome.agentcore.AgentInitializer;
import org.autonome.agentcore.LLMClient;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ResumeParserAgent implements Agent, AgentInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(ResumeParserAgent.class);
    
    @Autowired
    @Qualifier("claudeLLMClient")
    private LLMClient llmClient;
    
    private final ObjectMapper objectMapper;
    private AgentDefinition definition;
    private List<AgentExtension> extensions;
    
    public ResumeParserAgent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void init(AgentDefinition definition, List<AgentExtension> extensions) {
        this.definition = definition;
        this.extensions = extensions;
    }
    
    @Override
    public String getName() {
        return definition != null ? definition.getName() : "resume-parser-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String resumeText = (String) exchange.getIn().getHeader("resume_text");
        
        logger.info("Parsing resume ({} characters)", resumeText.length());
        
        // TODO: Build prompt to extract structured data from resume
        // Hint: Ask Claude to extract: name, email, phone, experience, education, etc.
        String prompt = buildParsePrompt(resumeText);
        
        // TODO: Configure and call LLM
        Map<String, Object> config = new HashMap<>();
        config.put("systemPrompt", getSystemPrompt());
        config.put("model", context.get("model", String.class) != null ? context.get("model", String.class) : "claude-3-5-sonnet-20241022");
        config.put("temperature", context.get("temperature", Double.class) != null ? context.get("temperature", Double.class) : 0.3);
        config.put("max_tokens", context.get("max_tokens", Integer.class) != null ? context.get("max_tokens", Integer.class) : 2000);
        
        String llmResponse = llmClient.callLLM(prompt, null, config);
        
        // TODO: Parse JSON response
        Map<String, Object> parsedData = objectMapper.readValue(cleanJsonResponse(llmResponse), Map.class);
        
        logger.info("Successfully parsed resume for: {}", parsedData.get("name"));
        
        exchange.getMessage().setBody(parsedData);
    }
    
    private String buildParsePrompt(String resumeText) {
        return "Parse this resume and extract structured information:\n\n" + resumeText;
    }
    
    private String getSystemPrompt() {
        if (definition != null && definition.getSystemPrompt() != null) {
            return definition.getSystemPrompt();
        }
        return "You are a resume parser. Extract structured data and return valid JSON.";
    }
    
    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }
}

 Your Task:

Fill in buildParsePrompt() to ask Claude to extract: name, contact info, experience, education, skills
Use the system prompt in agents.yaml to guide the output format
Test with different resume formats

Step 2: Create SkillExtractorAgent
File: src/main/java/org/autonome/community/agents/SkillExtractorAgent.java

package org.autonome.community.agents;

import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SkillExtractorAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(SkillExtractorAgent.class);
    
    @Override
    public String getName() {
        return "skill-extractor-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        // TODO: Get parsed_data from context
        @SuppressWarnings("unchecked")
        Map<String, Object> parsedData = (Map<String, Object>) context.get("parsed_data");
        
        logger.info("Extracting skills from parsed resume data");
        
        // TODO: Use LLM to categorize skills (technical, soft, tools, languages)
        // TODO: Return structured skill list
        
        // HINT: You'll need to inject LLMClient similar to ResumeParserAgent
        // HINT: Build a prompt that takes parsed_data and categorizes skills
        
        throw new UnsupportedOperationException("TODO: Implement SkillExtractorAgent");
    }
}
 Your Task:

Inject LLMClient (copy pattern from ResumeParserAgent)
Build prompt that categorizes skills into: technical, soft skills, tools, languages
Parse JSON response with categories
Set result in exchange body

Step 3: Create SummaryGeneratorAgent
File: src/main/java/org/autonome/community/agents/SummaryGeneratorAgent.java

package org.autonome.community.agents;

import java.util.Map;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SummaryGeneratorAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(SummaryGeneratorAgent.class);
    
    @Override
    public String getName() {
        return "summary-generator-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        // TODO: Get parsed_data and skills from context
        Map<String, Object> parsedData = (Map<String, Object>) context.get("parsed_data");
        Map<String, Object> skills = (Map<String, Object>) context.get("skills");
        
        logger.info("Generating professional summary");
        
        // TODO: Use LLM to generate 2-3 sentence professional summary
        // TODO: Highlight key strengths, experience, and notable achievements
        
        throw new UnsupportedOperationException("TODO: Implement SummaryGeneratorAgent");
    }
}

Your Task:

Inject LLMClient
Build prompt using both parsed_data and skills
Generate concise 2-3 sentence summary
Return as string in exchange body

Step 4: Add Agents to agents.yaml
File: flows/agents.yaml
Add these three agent definitions:

- agentId: resumeParserAgent
  name: Resume Parser Agent
  systemPrompt: |
    You are an expert resume parser. Extract structured information from resumes.
    
    Extract the following fields:
    - name (full name)
    - contact (email, phone, location)
    - experience (array of job entries with title, company, dates, description)
    - education (array of degrees with school, degree, field, year)
    - skills (array of skill names mentioned)
    
    Return ONLY valid JSON with this structure:
    {
      "name": "...",
      "contact": {"email": "...", "phone": "...", "location": "..."},
      "experience": [{"title": "...", "company": "...", "dates": "...", "description": "..."}],
      "education": [{"school": "...", "degree": "...", "field": "...", "year": "..."}],
      "skills": ["skill1", "skill2"]
    }
    
    Handle missing sections gracefully with empty arrays.
  type: java
  config:
    class: org.autonome.community.agents.ResumeParserAgent
  enabledExtensions: []
  humanInLoopEnabled: false

- agentId: skillExtractorAgent
  name: Skill Extractor Agent
  systemPrompt: |
    You are a skill categorization expert. Analyze resume data and categorize skills.
    
    Categories:
    - technical: Programming languages, frameworks, architectures
    - tools: Software, platforms, IDEs
    - soft: Communication, leadership, problem-solving
    - languages: Human languages (English, Spanish, etc.)
    
    Return ONLY valid JSON:
    {
      "technical": ["Java", "Spring Boot"],
      "tools": ["Git", "Docker"],
      "soft": ["Leadership", "Communication"],
      "languages": ["English", "Spanish"]
    }
  type: java
  config:
    class: org.autonome.community.agents.SkillExtractorAgent
  enabledExtensions: []
  humanInLoopEnabled: false

- agentId: summaryGeneratorAgent
  name: Summary Generator Agent
  systemPrompt: |
    You are a professional resume writer. Generate a concise professional summary.
    
    Guidelines:
    - 2-3 sentences maximum
    - Highlight years of experience
    - Mention top 3-4 technical skills
    - Include one notable achievement or strength
    - Professional tone
    
    Return the summary as plain text (no JSON, no formatting).
  type: java
  config:
    class: org.autonome.community.agents.SummaryGeneratorAgent
  enabledExtensions: []
  humanInLoopEnabled: false

  Step 5: Create REST Controller
File: src/main/java/org/autonome/community/controllers/ResumeAnalysisController.java

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

@RestController
@RequestMapping("/api/demo/resume-analysis")
@CrossOrigin(origins = "*")
public class ResumeAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(ResumeAnalysisController.class);
    
    @Autowired
    private FlowRuntime flowRuntime;
    
    @Autowired
    private FlowLoader flowLoader;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> analyzeResume(@RequestBody Map<String, Object> request) {
        logger.info("Received resume analysis request");
        
        try {
            String resumeText = (String) request.get("resume_text");
            
            if (resumeText == null || resumeText.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "resume_text is required"));
            }
            
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("resume_text", resumeText);
            
            Flow flow = flowLoader.loadFromYaml("resume-analysis.flow.yaml");
            if (flow == null) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Flow not found: resume-analysis.flow.yaml"));
            }
            
            String conversationId = "demo_" + System.currentTimeMillis();
            AgentContext context = flowRuntime.run("demo", conversationId, flow, contextData);
            
            logger.info("Resume analysis completed successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "parsed_data", context.get("parsed_data"),
                "skills", context.get("skills"),
                "summary", context.get("summary")
            ));
            
        } catch (Exception e) {
            logger.error("Error in resume analysis", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

Step 6: Test Your Implementation
Create a test script:
File: test-resume-analysis.sh

#!/bin/bash

curl -X POST http://localhost:8080/api/demo/resume-analysis \
  -H "Content-Type: application/json" \
  -d '{
    "resume_text": "John Smith\nSenior Software Engineer\njohn.smith@email.com | (555) 123-4567\n\nEXPERIENCE\nSenior Software Engineer at TechCorp (2018-Present)\n- Led development of microservices architecture serving 10M users\n- Mentored team of 5 junior developers\n- Technologies: Java, Spring Boot, AWS, Kubernetes\n\nSoftware Engineer at StartupXYZ (2015-2018)\n- Built REST APIs and backend services\n- Improved system performance by 40%\n\nEDUCATION\nBS Computer Science, State University (2015)\n\nSKILLS\nJava, Spring Boot, Microservices, AWS, Kubernetes, Docker, PostgreSQL, Redis"
  }'

  Make it executable:

  chmod +x test-resume-analysis.sh

  {
  "success": true,
  "parsed_data": {
    "name": "John Smith",
    "contact": {
      "email": "john.smith@email.com",
      "phone": "(555) 123-4567"
    },
    "experience": [...],
    "education": [...]
  },
  "skills": {
    "technical": ["Java", "Spring Boot", "Microservices"],
    "tools": ["AWS", "Kubernetes", "Docker", "PostgreSQL", "Redis"],
    "soft": ["Leadership", "Mentoring"],
    "languages": []
  },
  "summary": "Senior Software Engineer with 10 years of experience specializing in Java, Spring Boot, and cloud-native microservices. Led development of high-scale distributed systems serving 10M users with expertise in AWS and Kubernetes. Strong technical leader with proven mentoring capabilities."
}

Success Criteria
✅ ResumeParserAgent extracts structured data
✅ SkillExtractorAgent categorizes skills correctly
✅ SummaryGeneratorAgent creates concise, professional summary
✅ API returns all three outputs
✅ Flow completes without errors
✅ Test script passes
Troubleshooting
Problem: "Agent not found"

Solution: Make sure agent is registered in agents.yaml and class name matches

Problem: "Context key not found"

Solution: Check that outputKey in flow matches the key you're trying to get

Problem: "JSON parsing error"

Solution: Add logging to see raw LLM response, adjust system prompt to enforce JSON format

Problem: "NullPointerException in agent"

Solution: Add null checks when getting data from context

Next Steps
Once working:

Add support for PDF resume parsing
Implement skill proficiency detection
Add industry-specific skill categorization
Generate tailored summaries for different roles

Learning Resources

Look at JobMatchScoringAgent.java for reference implementation
Study how ClaudeLLMClient is used
Review AgentContext API for data flow patterns