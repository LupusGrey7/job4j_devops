package ru.job4j.devops;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) // Avoid starting web server@ActiveProfiles("test")
class CalcApplicationTest {

    @Test
    void contextLoads() {
        System.out.println("test context");
    }
}