package ru.job4j.devops;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CalcApplicationMainTest {
    @Test
    void mainMethodTest() {
        System.setProperty("spring.profiles.active", "test");
        CalcApplication.main(new String[]{});
        Assertions.assertEquals("test", System.getProperty("spring.profiles.active"));
        System.clearProperty("spring.profiles.active");
    }
}
