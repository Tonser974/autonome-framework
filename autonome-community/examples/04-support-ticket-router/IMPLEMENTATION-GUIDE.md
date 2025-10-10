# Support Ticket Router - Implementation Guide

## Overview

Build an intelligent support ticket routing system that classifies tickets, routes them to the appropriate department, and generates draft responses. This demonstrates enterprise automation capabilities.

## What You'll Build

A 3-agent pipeline that:
1. **Classifies** ticket type and urgency
2. **Routes** to correct department
3. **Generates** draft response for agent

## Architecture

ticket_text → TicketClassifierAgent → classification
↓
RoutingAgent → department
↓
ResponseGeneratorAgent → draft_response

## Use Cases

- Customer support automation
- Help desk ticket triage
- IT service management
- Escalation handling
- Response time improvement

## Business Value

- **Reduce response time** by 60%
- **Improve routing accuracy** to 95%+
- **Decrease agent workload** by handling common queries
- **Ensure consistency** in responses
- **Track patterns** in customer issues

## Step 1: Create TicketClassifierAgent

**File:** `src/main/java/org/autonome/community/agents/TicketClassifierAgent.java`
```java
package org.autonome.community.agents;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TicketClassifierAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketClassifierAgent.class);
    
    @Override
    public String getName() {
        return "ticket-classifier-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        String ticketText = (String) exchange.getIn().getHeader("ticket_text");
        
        logger.info("Classifying support ticket");
        
        // TODO: Implement classification logic
        // HINT: Use LLM to classify into: technical, billing, general, complaint
        // HINT: Determine urgency: low, medium, high, critical
        // HINT: Identify category: bug, feature_request, question, etc.
        
        throw new UnsupportedOperationException("TODO: Implement TicketClassifierAgent");
    }
}

Your Task:

Inject LLMClient and ObjectMapper
Implement AgentInitializer interface
Build prompt that analyzes ticket text and customer info
Return JSON with: type, urgency, category, keywords

Step 2: Create RoutingAgent
File: src/main/java/org/autonome/community/agents/RoutingAgent.java

package org.autonome.community.agents;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RoutingAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutingAgent.class);
    
    @Override
    public String getName() {
        return "routing-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        // TODO: Get classification from context
        // TODO: Apply routing rules based on type and urgency
        // TODO: Return department and priority
        
        logger.info("Routing ticket to appropriate department");
        
        throw new UnsupportedOperationException("TODO: Implement RoutingAgent");
    }
}

Your Task:

Get classification data from context
Implement routing logic (can be rule-based or LLM-based)
Return: department, assigned_queue, priority, sla_hours

Step 3: Create ResponseGeneratorAgent
File: src/main/java/org/autonome/community/agents/ResponseGeneratorAgent.java

package org.autonome.community.agents;

import org.apache.camel.Exchange;
import org.autonome.api.Agent;
import org.autonome.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseGeneratorAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseGeneratorAgent.class);
    
    @Override
    public String getName() {
        return "response-generator-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        // TODO: Get ticket_text, classification, and department from context
        // TODO: Generate appropriate draft response
        // TODO: Tailor tone based on urgency and type
        
        logger.info("Generating draft response");
        
        throw new UnsupportedOperationException("TODO: Implement ResponseGeneratorAgent");
    }
}

Your Task:

Inject LLMClient
Build prompt with ticket context, classification, and department
Generate empathetic, helpful response
Include next steps and timeline

Step 4: Add Agent Configurations
Add to flows/agents.yaml:

- agentId: ticketClassifierAgent
  name: Ticket Classifier Agent
  systemPrompt: |
    You are an expert support ticket classifier. Analyze customer tickets and classify them accurately.
    
    Classification criteria:
    
    Type:
    - technical: System errors, bugs, integration issues
    - billing: Payments, invoices, refunds, pricing
    - general: Questions, how-to, information requests
    - complaint: Dissatisfaction, service issues, escalations
    
    Urgency:
    - critical: System down, data loss, security breach
    - high: Major functionality broken, blocking work
    - medium: Feature not working as expected, workaround available
    - low: Questions, minor issues, feature requests
    
    Return ONLY valid JSON:
    {
      "type": "technical|billing|general|complaint",
      "urgency": "critical|high|medium|low",
      "category": "bug|feature_request|question|account_issue|other",
      "keywords": ["keyword1", "keyword2"],
      "confidence": 0.0-1.0
    }
  type: java
  config:
    class: org.autonome.community.agents.TicketClassifierAgent
  enabledExtensions: []
  humanInLoopEnabled: false

- agentId: routingAgent
  name: Routing Agent
  systemPrompt: |
    You are a support ticket routing specialist. Route tickets to the correct department.
    
    Departments:
    - Engineering: Technical issues, bugs, system errors
    - Billing: Payments, invoices, account issues
    - Customer Success: Onboarding, training, general questions
    - Escalations: Complaints, urgent issues, executive requests
    
    Return ONLY valid JSON:
    {
      "department": "Engineering|Billing|Customer Success|Escalations",
      "assigned_queue": "tier1|tier2|tier3|priority",
      "priority": 1-5,
      "sla_hours": 24,
      "reasoning": "brief explanation"
    }
  type: java
  config:
    class: org.autonome.community.agents.RoutingAgent
  enabledExtensions: []
  humanInLoopEnabled: false

- agentId: responseGeneratorAgent
  name: Response Generator Agent
  systemPrompt: |
    You are a professional customer support agent. Generate helpful, empathetic draft responses.
    
    Guidelines:
    - Be empathetic and acknowledge the customer's concern
    - Provide clear next steps or solutions
    - Set expectations on timeline
    - Professional but friendly tone
    - Address the specific issue mentioned
    
    For technical issues: Offer troubleshooting steps
    For billing issues: Provide account review timeline
    For general questions: Give clear, helpful answers
    For complaints: Apologize sincerely, escalate appropriately
    
    Return the draft response as plain text (no JSON).
  type: java
  config:
    class: org.autonome.community.agents.ResponseGeneratorAgent
  enabledExtensions: []
  humanInLoopEnabled: false

  Step 5: Create REST Controller
File: src/main/java/org/autonome/community/controllers/TicketRouterController.java

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
@RequestMapping("/api/demo/ticket-router")
@CrossOrigin(origins = "*")
public class TicketRouterController {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketRouterController.class);
    
    @Autowired
    private FlowRuntime flowRuntime;
    
    @Autowired
    private FlowLoader flowLoader;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> routeTicket(@RequestBody Map<String, Object> request) {
        logger.info("Received ticket routing request");
        
        try {
            String ticketText = (String) request.get("ticket_text");
            @SuppressWarnings("unchecked")
            Map<String, Object> customerInfo = (Map<String, Object>) request.get("customer_info");
            
            if (ticketText == null || ticketText.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "ticket_text is required"));
            }
            
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("ticket_text", ticketText);
            contextData.put("customer_info", customerInfo != null ? customerInfo : Map.of());
            
            Flow flow = flowLoader.loadFromYaml("support-ticket-router.flow.yaml");
            if (flow == null) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Flow not found"));
            }
            
            String conversationId = "demo_" + System.currentTimeMillis();
            AgentContext context = flowRuntime.run("demo", conversationId, flow, contextData);
            
            logger.info("Ticket routing completed successfully");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "classification", context.get("classification"),
                "department", context.get("department"),
                "draft_response", context.get("draft_response")
            ));
            
        } catch (Exception e) {
            logger.error("Error in ticket routing", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

Step 6: Test Your Implementation
File: test-ticket-router.sh

#!/bin/bash

curl -X POST http://localhost:8080/api/demo/ticket-router \
  -H "Content-Type: application/json" \
  -d '{
    "ticket_text": "URGENT: Our entire production system is down. Users cannot login and we are losing money every minute. This has been happening for the last 30 minutes. Please help immediately!",
    "customer_info": {
      "customer_id": "CUST-12345",
      "plan": "enterprise",
      "account_manager": "Jane Smith"
    }
  }'

  Expected Output

  {
  "success": true,
  "classification": {
    "type": "technical",
    "urgency": "critical",
    "category": "bug",
    "keywords": ["production", "system down", "login", "urgent"],
    "confidence": 0.98
  },
  "department": {
    "department": "Engineering",
    "assigned_queue": "priority",
    "priority": 1,
    "sla_hours": 1,
    "reasoning": "Critical system outage affecting production requires immediate engineering response"
  },
  "draft_response": "Thank you for reaching out, and I sincerely apologize for the production system issues you're experiencing. I understand this is urgent and impacting your business.\n\nI've immediately escalated this to our senior engineering team as Priority 1. They are investigating the login system right now and will provide an update within 30 minutes.\n\nIn the meantime, our team is monitoring the situation in real-time. You should receive direct contact from our on-call engineer shortly.\n\nWe take system reliability extremely seriously and will keep you updated every 15 minutes until resolved."
}

Success Criteria
✅ TicketClassifierAgent accurately classifies tickets
✅ RoutingAgent routes to correct department
✅ ResponseGeneratorAgent creates appropriate responses
✅ Critical tickets get priority routing
✅ Response tone matches urgency level
✅ All outputs returned correctly
Advanced Features to Add

Multi-language support
Customer sentiment analysis
Similar ticket detection
Auto-response for common issues
Escalation path recommendations
SLA tracking and alerts

Integration Ideas

Connect to Zendesk/Freshdesk API
Auto-create tickets in tracking system
Send notifications to Slack
Log routing decisions for analytics
A/B test response quality

Learning Objectives

Multi-stage decision making
Rule-based + AI hybrid systems
Context passing between agents
Enterprise workflow automation
Customer-facing AI responses

This is a TODO example - implement the agents yourself! 🚀
Use JobMatchScoringAgent.java as your reference implementation.