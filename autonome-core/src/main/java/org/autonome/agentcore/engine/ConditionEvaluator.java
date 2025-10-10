package org.autonome.agentcore.engine;

import java.util.Map;

import org.autonome.context.AgentContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ConditionEvaluator {

    private static final ExpressionParser parser = new SpelExpressionParser();

    public static boolean evaluate(String condition, AgentContext context, Map<String, Object> globals) {
        System.out.println("🧪 Evaluating condition: [" + condition + "]");

        if (condition == null || condition.trim().isEmpty()) {
            return true; // No condition means always true
        }

        // Create a StandardEvaluationContext to hold variables for evaluation
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        // Make AgentContext data available to SpEL
        // You can expose the entire context object, or just its 'data' and 'conversationLog' map
        // For simplicity, let's expose 'data' and 'globals' directly as maps named 'data' and 'globals'
        evaluationContext.setVariable("data", context.getData());
        evaluationContext.setVariable("globals", globals);

        // Optionally, if you want direct access to context methods (like context.getTenantId())
        // evaluationContext.setRootObject(context); // Then conditions could be like: "#root.tenantId == 'my-website'"

        try {
            // Parse the expression (e.g., "#data.job_description != null && !#data.job_description.isEmpty()")
            // Note: We need to prefix context variables with #data. or #globals.
            // Or if you expose context directly, use #root.
            Expression exp = parser.parseExpression(condition);

            // Evaluate the expression
            return exp.getValue(evaluationContext, Boolean.class);

        } catch (Exception e) {
            System.err.println("⚠️ Error evaluating condition: " + condition + ". Error: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return false; // Condition evaluation failed, default to false (skip task)
        }
    }

    // In org.autonome.agentcore.engine.ConditionEvaluator.java
// ... (existing imports and evaluate method) ...

    // NEW: Generic getValue method for SpEL
    public static <T> T getValue(String expressionString, AgentContext context, Map<String, Object> globals, Class<T> returnType) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setVariable("data", context.getData());
        evaluationContext.setVariable("globals", globals); // Make globals available too

        Expression exp = parser.parseExpression(expressionString);
        return exp.getValue(evaluationContext, returnType);
    }
}