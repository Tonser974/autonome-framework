# Autonome

**Enterprise AI Agent Orchestration for Java/Spring Boot**

Autonome is a production-grade framework for building and orchestrating AI agent workflows in Java/Spring applications. Build complex AI-powered systems with type-safety, Spring Boot integration, and enterprise reliability.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4%2B-green.svg)]()

---

## Why Autonome?

### **Built for Enterprise Java**
- **Pure Java/Spring Boot** - Integrates seamlessly with your existing infrastructure
- **Type-safe workflows** - Compile-time checking, no runtime surprises
- **Apache Camel native** - 300+ enterprise connectors out of the box
- **Production-ready** - Thread-safe, auditable, scalable

### **YAML-Driven Workflows**
Define complex multi-agent workflows in simple YAML:

```yaml
id: customer-onboarding
type: sequential
taskDefinitions:
  - id: validate-data
    agentId: validation-agent
    outputKey: validation_result
  
  - id: create-account
    agentId: crm-agent
    condition: "${validation_result == 'valid'}"
```

### **Flexible Execution Strategies**
- **Sequential** - Step-by-step workflows
- **Parallel** - Concurrent task processing
- **Conversational** - Stateful multi-turn dialogues

### **Enterprise Ready**
- Multi-tenant isolation
- Pluggable context stores (In-Memory, PostgreSQL, Redis)
- Comprehensive logging and audit trails
- Designed for regulated industries (banking, healthcare, insurance)

---

## Quick Start

### 1. Add Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.autonome</groupId>
        <artifactId>autonome-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-spring-boot-starter</artifactId>
        <version>4.2.0</version>
    </dependency>
</dependencies>
```

### 2. Create Your First Agent

```java
@Component
public class CustomerValidationAgent implements Agent {
    
    @Override
    public String getName() {
        return "customer-validation-agent";
    }
    
    @Override
    public void handle(Exchange exchange, AgentContext context) {
        String customerId = (String) exchange.getIn().getHeader("customer_id");
        boolean isValid = validateCustomer(customerId);
        exchange.getMessage().setBody(isValid ? "valid" : "invalid");
    }
}
```

### 3. Define a Flow

```yaml
# flows/customer-onboarding.yaml
id: customer-onboarding
name: Customer Onboarding Flow
type: sequential

taskDefinitions:
  - id: validate
    agentId: customer-validation-agent
    input:
      customer_id: "${customer_id}"
    outputKey: validation_result
  
  - id: create-account
    agentId: crm-agent
    condition: "${validation_result == 'valid'}"
    input:
      customer_id: "${customer_id}"
    outputKey: account_id
```

### 4. Execute the Flow

```java
@Autowired
private FlowRuntime flowRuntime;

@Autowired
private FlowLoader flowLoader;

public void onboardCustomer(String customerId) {
    Flow flow = flowLoader.loadFromYaml("customer-onboarding.yaml");
    Map<String, Object> input = Map.of("customer_id", customerId);
    
    AgentContext context = flowRuntime.run(
        "tenant-123", 
        "conversation-456", 
        flow, 
        input
    );
    
    String accountId = (String) context.get("account_id");
    System.out.println("Created account: " + accountId);
}
```

---

## Complete Example: AI-Powered Job Matching

**See it in action:** `autonome-community/examples/01-job-match-scoring/`

### Flow Definition

```yaml
id: job-match-scoring
type: sequential
description: AI-powered candidate-job match scoring

globals:
  model: claude-3-5-sonnet-20241022
  temperature: 0.3
  max_tokens: 2000

taskDefinitions:
  - id: score-match
    agentId: jobMatchScoringAgent
    input:
      job_data: "${job_data}"
      candidate_data: "${candidate_data}"
    outputKey: match_result
```

### Agent Implementation

```java
@Component
public class JobMatchScoringAgent implements Agent, AgentInitializer {
    
    @Autowired
    @Qualifier("claudeLLMClient")
    private LLMClient llmClient;
    
    @Override
    public void handle(Exchange exchange, AgentContext context) throws Exception {
        Map<String, Object> jobData = (Map) exchange.getIn().getHeader("job_data");
        Map<String, Object> candidateData = (Map) exchange.getIn().getHeader("candidate_data");
        
        String prompt = buildMatchPrompt(jobData, candidateData);
        String response = llmClient.callLLM(prompt, null, config);
        
        Map<String, Object> result = parseMatchResult(response);
        exchange.getMessage().setBody(result);
    }
}
```

### REST API

```java
@PostMapping("/api/demo/job-match")
public ResponseEntity<Map<String, Object>> scoreMatch(@RequestBody Map<String, Object> request) {
    Flow flow = flowLoader.loadFromYaml("job-match-scoring.flow.yaml");
    AgentContext context = flowRuntime.run("demo", conversationId, flow, request);
    return ResponseEntity.ok(Map.of("result", context.get("match_result")));
}
```

### Test It

```bash
curl -X POST http://localhost:8080/api/demo/job-match \
  -H "Content-Type: application/json" \
  -d '{
    "job_data": {
      "title": "Senior Java Developer",
      "required_skills": "Java, Spring Boot, Microservices"
    },
    "candidate_data": {
      "name": "Jane Smith",
      "skills": "Java, Spring Boot, AWS, Kubernetes"
    }
  }'
```

### Response

```json
{
  "success": true,
  "result": {
    "total_score": 92,
    "strengths": [
      "Strong Java and Spring Boot expertise",
      "Relevant cloud-native experience",
      "Leadership capabilities"
    ],
    "recommendation": "hire",
    "summary": "Excellent technical fit with strong Java/Spring background..."
  }
}
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    YAML Flow Definition                      │
│  (Sequential | Parallel | Conversational)                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     FlowExecutor                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Sequential  │  │   Parallel   │  │Conversational│      │
│  │    Engine    │  │    Engine    │  │    Engine    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    AgentExecutor                             │
│  • Task Input Resolution  • Agent Instantiation             │
│  • Context Management     • Result Storage                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Agent Implementations                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Built-in     │  │  Custom      │  │  Apache      │      │
│  │ Agents       │  │  Agents      │  │  Camel       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              Context Store (Pluggable)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  In-Memory   │  │  PostgreSQL  │  │   Custom     │      │
│  │  (Default)   │  │  (Opt-in)    │  │  (Redis...)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Features

### **Multiple Execution Strategies**

**Sequential Flows**
```yaml
type: sequential
taskDefinitions:
  - id: step1
    agentId: data-loader
  - id: step2
    agentId: data-processor
  - id: step3
    agentId: data-saver
```

**Parallel Processing**
```yaml
type: parallel
taskDefinitions:
  - id: fetch-user-data
    agentId: user-service
  - id: fetch-order-data
    agentId: order-service
  - id: fetch-payment-data
    agentId: payment-service
```

**Conversational Agents**
```yaml
type: conversational
taskDefinitions:
  - id: chat
    agentId: customer-support-agent
    input:
      user_message: "${user_input}"
```

### **Conditional Execution**

```yaml
taskDefinitions:
  - id: check-inventory
    agentId: inventory-agent
    outputKey: stock_level
  
  - id: reorder
    agentId: purchase-agent
    condition: "${stock_level < 10}"
```

### **Subflow Composition**

```yaml
taskDefinitions:
  - id: validate-order
    flowRef: order-validation-flow
  
  - id: process-payment
    flowRef: payment-flow
    condition: "${validation_passed}"
```

### **Enterprise Integration**

```java
@Component
public class SalesforceAgent implements Agent {
    
    @Autowired
    private ProducerTemplate camelProducer;
    
    @Override
    public void handle(Exchange exchange, AgentContext context) {
        // Use any of 300+ Camel connectors
        String result = camelProducer.requestBody(
            "salesforce:query?sObjectQuery=SELECT Name FROM Account",
            null,
            String.class
        );
        exchange.getMessage().setBody(result);
    }
}
```

---

## Examples

Autonome Community includes 4 complete examples:

### **1. Job Match Scoring** ✅ Complete & Working
AI-powered candidate evaluation with Claude API
- **Location:** `autonome-community/examples/01-job-match-scoring/`
- **What it does:** Scores candidates against job requirements
- **Status:** Fully implemented with REST API

### **2. Resume Analysis Pipeline** 📝 Implementation Guide
Multi-step resume parsing, skill extraction, and summarization
- **Location:** `autonome-community/examples/02-resume-analysis/`
- **What it does:** Parse → Extract Skills → Generate Summary
- **Status:** Flow YAML + step-by-step guide

### **3. Text Processing Pipeline** 📝 Implementation Guide
Generic text cleaning, sentiment analysis, and summarization
- **Location:** `autonome-community/examples/03-text-processing/`
- **What it does:** Clean → Analyze Sentiment → Summarize
- **Status:** Flow YAML + step-by-step guide

### **4. Support Ticket Router** 📝 Implementation Guide
Intelligent ticket classification, routing, and response generation
- **Location:** `autonome-community/examples/04-support-ticket-router/`
- **What it does:** Classify → Route → Generate Draft Response
- **Status:** Flow YAML + step-by-step guide

**Try the complete example:**
```bash
cd autonome-community
mvn spring-boot:run
./test-job-match.sh
```

---

## Built-In Agents

Autonome includes production-ready agents:

| Agent | Purpose | Use Case |
|-------|---------|----------|
| **LoadDocAgent** | Document loading | Read files for processing |
| **ScanFolderAgent** | Directory scanning | Batch file operations |
| **OpenAIEmbeddingAgent** | Text embeddings | RAG pipelines, semantic search |
| **JsonBuilderAgent** | JSON transformation | Data format conversion |

---

## Configuration

### Basic Configuration

```yaml
# application.yml
autonome:
  agents:
    path: agents.yaml
  flows:
    dir: flows/
  context:
    store: in-memory  # or 'postgres'

# LLM Configuration
anthropic:
  api:
    key: ${ANTHROPIC_API_KEY}

# Logging
logging:
  level:
    org.autonome: INFO
```

### Agent Definition

```yaml
# agents.yaml
- agentId: my-custom-agent
  name: My Custom Agent
  systemPrompt: |
    You are an expert at...
  type: java
  config:
    class: com.example.MyCustomAgent
  enabledExtensions: []
  humanInLoopEnabled: false
```

---

## Production Deployment

### 1. Use PostgreSQL Context Store
```yaml
autonome:
  context:
    store: postgres

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/autonome
    username: autonome_user
    password: ${DB_PASSWORD}
```

### 2. Configure Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 3. Enable Structured Logging
```yaml
logging:
  level:
    org.autonome: INFO
  file:
    name: /var/log/autonome/app.log
```

### 4. Run
```bash
java -jar autonome-community-1.0.0.jar
```

---

## Use Cases

### **Banking & Financial Services**
- KYC/AML workflows with audit trails
- Loan processing pipelines
- Fraud detection orchestration

### **Healthcare**
- Patient onboarding flows
- Claims processing automation
- Regulatory compliance checks

### **E-commerce**
- Order fulfillment pipelines
- Inventory management
- Customer support automation

### **Insurance**
- Policy underwriting workflows
- Claims adjudication
- Risk assessment pipelines

### **Recruiting & HR**
- Candidate screening and matching
- Resume parsing and analysis
- Interview scheduling automation

---

## Testing

Autonome includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run specific module
cd autonome-core
mvn test
```

**Test Coverage:**
- ✅ Context storage and thread-safety
- ✅ Flow orchestration engines
- ✅ YAML loading and validation
- ✅ Agent lifecycle management
- ✅ Integration tests

---

## Module Structure

```
autonome-parent/
├── autonome-api/          # Core interfaces and contracts
├── autonome-core/         # Engine, executors, built-in agents
└── autonome-community/    # Examples, demos, getting started
```

**autonome-api** - Type-safe interfaces for agents, flows, context
**autonome-core** - Execution engines, flow loaders, built-in agents
**autonome-community** - Working examples and implementation guides

---

## Community & Support

### **Getting Help**
- 📖 **Documentation** - Check the `/docs` folder and example READMEs
- 💬 **Discussions** - Ask questions on GitHub Discussions
- 🐛 **Issues** - Report bugs on GitHub Issues

### **Contributing**
We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

- Found a bug? Open an issue
- Have a feature idea? Start a discussion
- Want to contribute code? Submit a pull request

### **Enterprise Support**
Need help implementing Autonome in your organization?

We offer:
- Quick start packages
- Custom agent development
- Enterprise integration consulting
- Performance optimization
- Training and workshops

---

## Roadmap

- [ ] **Enhanced Observability** - Distributed tracing, metrics dashboard
- [ ] **Flow Versioning** - Git-based version control for flows
- [ ] **Visual Flow Builder** - Web UI for designing workflows
- [ ] **More Built-In Agents** - Slack, Teams, email connectors
- [ ] **Python Bridge** - Call Python agents from Java workflows
- [ ] **Kubernetes Operator** - Native K8s deployment and scaling

---

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.

---

## Credits

Built for the enterprise Java community.

**Core Technologies:**
- Spring Boot 3.4+
- Apache Camel 4.2+
- Java 17+

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/aibroughttolife/autonome-framework.git

# Build all modules
cd autonome (or parent folder)
mvn clean install

# Run the community examples
cd autonome-community
java -jar target/autonome-community-1.0.0.jar

# Test the Job Match API
./test-job-match.sh
```

**⭐ Star this repo if you find it useful!**

---

**Ready to build production-grade AI workflows in Java?**

Start with the [Job Match Scoring example](autonome-community/examples/01-job-match-scoring/) and see Autonome in action.