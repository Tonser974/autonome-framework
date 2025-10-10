#!/bin/bash

# Test script for Job Match Scoring example
# Usage: ./test-job-match.sh

echo "🎯 Testing Autonome Job Match Scoring"
echo "======================================"
echo ""

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

echo ""
echo ""
echo "✅ Test completed"
echo ""
echo "💡 For formatted output, pipe through jq:"
echo "   ./test-job-match.sh | jq ."