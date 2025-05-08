package ru.job4j.develop.integration.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import ru.job4j.devops.CalcApplication;

@ActiveProfiles("integration")
@SpringBootTest(
        classes = CalcApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class ContainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine"
            ).withReuse(true);

    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.2")
    ).withReuse(true);

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    public static final String POSTGRES_DB = POSTGRES.getDatabaseName();
    public static final String POSTGRES_USER = POSTGRES.getUsername();
    public static final String POSTGRES_PASSWORD = POSTGRES.getPassword();
    public static final Integer POSTGRES_PORT = POSTGRES.getFirstMappedPort();
    public static final Integer KAFKA_PORT = POSTGRES.getFirstMappedPort();

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}

