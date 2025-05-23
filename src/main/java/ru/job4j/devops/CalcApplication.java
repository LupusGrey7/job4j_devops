package ru.job4j.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class CalcApplication {

	public static void main(String[] args) {
//		ConfigurableApplicationContext context = SpringApplication.run(CalcApplication.class, args);
//		Environment env = context.getEnvironment();
//		System.out.println("SPRING_DATASOURCE_URL: " + env.getProperty("SPRING_DATASOURCE_URL"));
//		System.out.println("DB_USERNAME: " + env.getProperty("DB_USERNAME"));
		SpringApplication.run(CalcApplication.class, args);

	}
}
