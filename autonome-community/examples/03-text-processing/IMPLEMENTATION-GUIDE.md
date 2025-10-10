# Text Processing Pipeline - Implementation Guide

## Overview

Build a generic text processing pipeline that cleans, analyzes sentiment, and summarizes any text content.

## What You'll Build

A 3-agent pipeline that:
1. **Cleans** raw text (remove formatting, normalize spacing)
2. **Analyzes** sentiment (positive, negative, neutral)
3. **Summarizes** the cleaned text with sentiment context

## Architecture

raw_text → TextCleanerAgent → cleaned_text
↓
SentimentAnalyzerAgent → sentiment
↓
SummarizerAgent → summary

## Implementation Instructions

### Step 1: Create the Three Agents

Create these three Java files in `src/main/java/org/autonome/community/agents/`:

1. **TextCleanerAgent.java** - Cleans and normalizes text
2. **SentimentAnalyzerAgent.java** - Analyzes sentiment
3. **SummarizerAgent.java** - Generates summary

**💡 Your Task:**
- Follow the same pattern as `JobMatchScoringAgent.java`
- Inject `LLMClient` using `@Qualifier("claudeLLMClient")`
- Implement `AgentInitializer` interface
- Use system prompts from `agents.yaml`

### Step 2: Add Agent Configurations

Add to `flows/agents.yaml`:
```yaml
- agentId: textCleanerAgent
  name: Text Cleaner Agent
  systemPrompt: |
    You are a text cleaning specialist. Clean and normalize text for analysis.
    
    Tasks:
    - Remove excessive whitespace and newlines
    - Fix common typos and formatting issues
    - Preserve meaning and structure
    - Remove special characters if inappropriate
    
    Return the cleaned text as plain text (no JSON, no markdown).
  type: java
  config:
    class: org.autonome.community.agents.TextCleanerAgent
  enabledExtensions: []
  humanInLoopEnabled: false

- agentId: sentimentAnalyzerAgent
  name: Sentiment Analyzer Agent
  systemPrompt: |
    You are a sentiment analysis expert. Analyze the emotional tone of text.
    
    Return ONLY valid JSON:
    {
      "sentiment": "positive|negative|neutral|mixed",
      "confidence": 0.0-1.0,
      "emotions": ["joy", "anger", "sadness", etc.],
      "reasoning": "brief explanation"
    }
  type: java
  config:
    class: org.autonome.community.agents.SentimentAnalyzerAgent
  enabledExtensions: []
  humanInLoopEnabled: false

- agentId: summarizerAgent
  name: Summarizer Agent
  systemPrompt: |
    You are a text summarization expert. Create concise summaries.
    
    Guidelines:
    - Maximum 3 sentences
    - Capture key points and main ideas
    - Incorporate sentiment context if relevant
    - Maintain factual accuracy
    
    Return the summary as plain text.
  type: java
  config:
    class: org.autonome.community.agents.SummarizerAgent
  enabledExtensions: []
  humanInLoopEnabled: false

  Step 3: Create REST Controller
File: src/main/java/org/autonome/community/controllers/TextProcessingController.java

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
@RequestMapping("/api/demo/text-processing")
@CrossOrigin(origins = "*")
public class TextProcessingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TextProcessingController.class);
    
    @Autowired
    private FlowRuntime flowRuntime;
    
    @Autowired
    private FlowLoader flowLoader;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> processText(@RequestBody Map<String, Object> request) {
        logger.info("Received text processing request");
        
        try {
            String rawText = (String) request.get("raw_text");
            
            if (rawText == null || rawText.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "raw_text is required"));
            }
            
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("raw_text", rawText);
            
            Flow flow = flowLoader.loadFromYaml("text-processing.flow.yaml");
            if (flow == null) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Flow not found"));
            }
            
            String conversationId = "demo_" + System.currentTimeMillis();
            AgentContext context = flowRuntime.run("demo", conversationId, flow, contextData);
            
            logger.info("Text processing completed successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cleaned_text", context.get("cleaned_text"),
                "sentiment", context.get("sentiment"),
                "summary", context.get("summary")
            ));
            
        } catch (Exception e) {
            logger.error("Error in text processing", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

Step 4: Test Your Implementation
File: test-text-processing.sh

#!/bin/bash

curl -X POST http://localhost:8080/api/demo/text-processing \
  -H "Content-Type: application/json" \
  -d '{
    "raw_text": "This    product   is    AMAZING!!!   I    absolutely    LOVE   it.   Best    purchase    ever.    Highly    recommend    to    everyone.    The    quality    is    outstanding    and    customer    service    was    excellent."
  }'

  Expected Output

  {
  "success": true,
  "cleaned_text": "This product is AMAZING! I absolutely LOVE it. Best purchase ever. Highly recommend to everyone. The quality is outstanding and customer service was excellent.",
  "sentiment": {
    "sentiment": "positive",
    "confidence": 0.95,
    "emotions": ["joy", "satisfaction", "enthusiasm"],
    "reasoning": "Strong positive language with superlatives and explicit recommendations"
  },
  "summary": "Customer expresses extreme satisfaction with product purchase, praising its outstanding quality and excellent customer service. Highly recommends to others."
}

Success Criteria
✅ TextCleanerAgent normalizes text properly
✅ SentimentAnalyzerAgent detects sentiment accurately
✅ SummarizerAgent creates concise summary
✅ All three outputs returned correctly
✅ Test script passes
Learning Objectives

Text preprocessing techniques
Sentiment analysis with LLMs
Multi-stage data transformation
Generic reusable agents

Next Steps

Add language detection
Support multiple languages
Add emotion intensity scoring
Implement topic extraction