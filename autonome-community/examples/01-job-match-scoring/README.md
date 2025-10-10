# Job Match Scoring Example

**AI-powered candidate-job matching using Autonome**

This is a **complete, working example** that demonstrates how to build an intelligent job matching system using the Autonome framework.

## 🎯 What It Does

Analyzes candidate profiles against job postings and provides:
- **Match score** (0-100) based on skills, experience, and fit
- **Strengths** that make the candidate a good fit
- **Gaps** where the candidate may need development
- **Recommendation** (hire, interview, or pass)
- **Summary** with actionable insights for recruiters

## 🏗️ Architecture

Request → JobMatchController → FlowRuntime → JobMatchScoringAgent → Claude API
↓
AgentContext (stores result)

### Components

1. **JobMatchController.java** - REST API endpoint (`/api/demo/job-match`)
2. **JobMatchScoringAgent.java** - Core AI matching logic with LLM integration
3. **job-match-scoring.flow.yaml** - Flow orchestration definition
4. **agents.yaml** - Agent configuration with scoring methodology
5. **ClaudeLLMClient.java** - Anthropic Claude API client

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Anthropic API key

### 1. Configure API Key

Add to `application.properties` or set environment variable:
```properties
anthropic.api.key=sk-ant-your-key-here

Or:

export ANTHROPIC_API_KEY=sk-ant-your-key-here

2. Start the Server
cd autonome-community
mvn clean package
java -jar target/autonome-community-1.0.0.jar

You should see:

✅ Loaded 1 AgentDefinitions from YAML
🧠 jobMatchScoringAgent
✅ Tomcat started on port 8080

3. Test the API
Option A: Use the test script

./test-job-match.sh

Option B: Use curl

curl -X POST http://localhost:8080/api/demo/job-match \
  -H "Content-Type: application/json" \
  -d '{
    "job_data": {
      "title": "Senior Java Developer",
      "company": "TechCorp Inc",
      "required_skills": "Java 17+, Spring Boot, REST APIs, Microservices, PostgreSQL",
      "description": "We are looking for a Senior Java Developer to build scalable microservices for our fintech platform. You will lead technical design, mentor junior developers, and architect cloud-native solutions."
    },
    "candidate_data": {
      "name": "Jane Smith",
      "years_experience": 8,
      "skills": "Java, Spring Boot, Kubernetes, AWS, PostgreSQL, Redis, Kafka",
      "background": "Led development of payment processing system at major fintech startup. Architected migration from monolith to microservices (50+ services). Mentored team of 6 engineers. Expert in high-throughput distributed systems."
    }
  }'

  📊 Example Response

  {
  "success": true,
  "result": {
    "total_score": 92,
    "strengths": [
      "Deep fintech domain expertise with directly relevant payment processing experience",
      "Proven microservices architecture experience, having led 50+ service migration",
      "Strong technical leadership demonstrated through team mentorship of 6 engineers",
      "Comprehensive modern tech stack including required Java/Spring Boot plus valuable additions"
    ],
    "gaps": [
      "Java version expertise not explicitly specified - need to verify Java 17+ experience",
      "Years of experience (8) slightly below typical senior level expectation of 10+"
    ],
    "recommendation": "hire",
    "summary": "Exceptionally strong technical fit with directly relevant fintech and microservices expertise. Has led complex distributed systems work and demonstrated leadership through mentoring. While total years of experience is slightly below typical senior level, the depth of relevant experience and breadth of modern tech stack expertise more than compensates.",
    "analyzed_at": "2025-10-09T03:18:59.060023Z"
  }
}

🎓 How It Works

1. Flow Definition (job-match-scoring.flow.yaml)
id: job-match-scoring
name: Job Match Scoring Flow
type: sequential
description: AI-powered candidate-job match scoring

globals:
  model: claude-3-5-sonnet-20241022
  temperature: 0.3
  max_tokens: 2000

taskDefinitions:
  - id: score-match
    name: Score Candidate-Job Match
    agentId: jobMatchScoringAgent
    input:
      job_data: "${job_data}"
      candidate_data: "${candidate_data}"
    outputKey: match_result

Key concepts:

globals - Configuration shared across all tasks (model, temperature)
taskDefinitions - List of agents to execute
input - Data passed to agent using variable substitution
outputKey - Where to store the result in context

2. Agent Implementation
The JobMatchScoringAgent follows this pattern:

@Component
public class JobMatchScoringAgent implements Agent, AgentInitializer {
    
    @Autowired
    @Qualifier("claudeLLMClient")
    private LLMClient llmClient;
    
    @Override
    public void handle(Exchange exchange, AgentContext context) {
        // 1. Extract input data
        Map<String, Object> jobData = (Map) exchange.getIn().getHeader("job_data");
        Map<String, Object> candidateData = (Map) exchange.getIn().getHeader("candidate_data");
        
        // 2. Build analysis prompt
        String prompt = buildMatchPrompt(jobData, candidateData);
        
        // 3. Configure and call LLM
        Map<String, Object> config = new HashMap<>();
        config.put("systemPrompt", getSystemPrompt());
        config.put("model", context.get("model", String.class));
        String llmResponse = llmClient.callLLM(prompt, null, config);
        
        // 4. Parse JSON response
        Map<String, Object> result = parseMatchResult(llmResponse);
        
        // 5. Return result
        exchange.getMessage().setBody(result);
    }
}

3. Scoring Methodology
Defined in agents.yaml system prompt:

systemPrompt: |
  SCORING METHODOLOGY (Total: 100 points):
  
  1. SKILLS ALIGNMENT (40 points)
     - Required skills match and proficiency
     - Bonus for additional relevant skills
  
  2. EXPERIENCE RELEVANCE (30 points)
     - Years of experience alignment
     - Industry/domain experience match
  
  3. CULTURAL FIT INDICATORS (20 points)
     - Leadership and mentorship experience
     - Communication and collaboration signals
  
  4. GROWTH POTENTIAL (10 points)
     - Learning trajectory and technology adoption

Customization
Modify Scoring Criteria
Edit the system prompt in flows/agents.yaml:

- agentId: jobMatchScoringAgent
  systemPrompt: |
    # Add your custom scoring logic here
    - Industry experience (25 points)
    - Certifications (15 points)
    - Location match (10 points)

Add New Input Fields
Update the agent to accept additional data:

String preferredLocation = (String) jobData.get("location");
String candidateLocation = (String) candidateData.get("location");

Then include in the prompt:

prompt.append("Job Location: ").append(preferredLocation).append("\n");
prompt.append("Candidate Location: ").append(candidateLocation).append("\n");

Change LLM Model
Update in job-match-scoring.flow.yaml:

globals:
  model: gpt-4  # or claude-opus-4, etc.

Or override in the request:

curl -X POST http://localhost:8080/api/demo/job-match \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4",
    "job_data": {...}
  }'


📁 File Structure
  autonome-community/
├── src/main/java/org/autonome/community/
│   ├── agents/
│   │   ├── JobMatchScoringAgent.java      ✅ Complete
│   │   └── ClaudeLLMClient.java           ✅ Complete
│   └── controllers/
│       └── JobMatchController.java        ✅ Complete
│
├── flows/
│   ├── agents.yaml                        ✅ Complete
│   └── job-match-scoring.flow.yaml       ✅ Complete
│
└── test-job-match.sh                      ✅ Complete

Testing
Unit Tests
Test individual components:

@Test
void testJobMatchScoring() {
    Map<String, Object> jobData = Map.of(
        "title", "Java Developer",
        "required_skills", "Java, Spring"
    );
    
    Map<String, Object> candidateData = Map.of(
        "name", "Test Candidate",
        "skills", "Java, Spring Boot, AWS"
    );
    
    // Test agent logic
}

Integration Tests
Test the full flow:

# Good match (should score 80+)
./test-job-match.sh

# Poor match (should score <50)
curl -X POST http://localhost:8080/api/demo/job-match \
  -H "Content-Type: application/json" \
  -d '{
    "job_data": {
      "title": "Senior Data Scientist",
      "required_skills": "Python, TensorFlow, ML"
    },
    "candidate_data": {
      "name": "Java Dev",
      "skills": "Java, Spring Boot"
    }
  }'

   Use Cases

Applicant Tracking Systems (ATS) - Screen candidates automatically
Job Recommendations - Match candidates to open positions
Skill Gap Analysis - Identify training needs
Talent Pool Mining - Find hidden gems in candidate database
Interview Prep - Generate focus areas for interviewers
Diversity Hiring - Reduce unconscious bias with objective scoring

🎯 Key Learning Points
This example demonstrates:

✅ Agent Pattern - Encapsulated business logic with clear interface
✅ Flow Orchestration - YAML-based workflow definition
✅ LLM Integration - Claude API with structured JSON output
✅ Context Management - Passing data between agents
✅ REST API Design - Clean Spring Boot controller
✅ Configuration - Externalized prompts in YAML
✅ Error Handling - Graceful degradation and logging
✅ Type Safety - Proper Java generics usage

🔗 Related Examples

Resume Analysis Pipeline - Multi-step resume processing (TODO: Implement)
Text Processing Pipeline - Generic NLP workflow (TODO: Implement)
Support Ticket Router - Enterprise automation (TODO: Implement)

📚 Documentation

Autonome Core Documentation
Creating Custom Agents
Flow Configuration Guide
LLM Integration Guide

🤝 Contributing
Found a bug or have an improvement?

Open an issue
Submit a pull request
Share your use case

📄 License
Apache 2.0 - See LICENSE file

This is a complete, working example. Use it as a reference for building your own agents!