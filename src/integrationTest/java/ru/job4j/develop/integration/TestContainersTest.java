package ru.job4j.develop.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersTest.class);

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        POSTGRES.start();
    }

    @AfterAll
    static void afterAll() {
        POSTGRES.stop();
    }

    @Test
    public void whenSaveUser() {
        LOGGER.info("DB URL: {}", POSTGRES.getJdbcUrl());
        LOGGER.info("DB User name: {}", POSTGRES.getUsername());
        LOGGER.info("DB Password: {}",  POSTGRES.getPassword());
    }
}