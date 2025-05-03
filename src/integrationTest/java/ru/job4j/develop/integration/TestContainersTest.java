package ru.job4j.develop.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.logging.Logger;

public class TestContainersTest {

    Logger LOGGER = Logger.getLogger(getClass().getName());


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
        LOGGER.info("DB URL: " + POSTGRES.getJdbcUrl());
        LOGGER.info("DB User name: " + POSTGRES.getUsername());
        LOGGER.info("DB Password: " + POSTGRES.getPassword());
    }
}