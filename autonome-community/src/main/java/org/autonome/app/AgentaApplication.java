package org.autonome.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"org.autonome", 
	"org.autonome.community.agents", 
	"org.autonome.config", 
	"org.autonome.agentcore", 
	"org.autonome.api", 
	"org.autonome.context", 
	"org.autonome.runtime"
	})		
public class AgentaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentaApplication.class, args);
	}

}
