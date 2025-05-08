package ru.job4j.develop.integration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.develop.integration.config.ContainersConfig;

@ActiveProfiles("integration")
public class TestContainersTest extends ContainersConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersTest.class);

    @Test
    public void whenSaveUser() {
        LOGGER.info("DB URL: {}", ContainersConfig.POSTGRES_DB);
        LOGGER.info("DB User name: {}", ContainersConfig.POSTGRES_USER);
        LOGGER.info("DB User password: {}", ContainersConfig.POSTGRES_PASSWORD);
        LOGGER.info("DB POSTGRES_PORT: {}", ContainersConfig.POSTGRES_PORT);
        LOGGER.info("Kafka URL: {}", ContainersConfig.KAFKA_PORT);
    }
}