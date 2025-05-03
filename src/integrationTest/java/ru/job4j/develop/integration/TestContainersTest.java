package ru.job4j.develop.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersTest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();

    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Test
    public void whenSaveUser() {
        System.out.println(postgres.getJdbcUrl());
        System.out.println(postgres.getUsername());
        System.out.println(postgres.getPassword());
    }
}