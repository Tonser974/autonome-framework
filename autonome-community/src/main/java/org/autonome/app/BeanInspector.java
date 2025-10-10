package org.autonome.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("inspect")
public class BeanInspector implements CommandLineRunner {
    @Autowired ApplicationContext context;

    @Override
    public void run(String... args) {
        for (String name : context.getBeanDefinitionNames()) {
            if (name.toLowerCase().contains("agent")) {
                System.out.println("🧠 " + name);
            }
        }
    }
}
