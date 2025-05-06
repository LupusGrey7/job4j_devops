package ru.job4j.develop.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.job4j.devops.CalcApplication;
import ru.job4j.devops.models.User;
import ru.job4j.devops.repository.CalcEventRepository;
import ru.job4j.devops.repository.UserRepository;
import ru.job4j.devops.service.CalcEventService;

@ActiveProfiles("integration")
@SpringBootTest(
        classes = CalcApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class CalcEventServiceIntegrationTest {

    @Autowired
    CalcEventService service;
    @Autowired
    private CalcEventRepository repository;
    @Autowired
    private UserRepository userRepository;

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ).withReuse(true);

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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
        int first = 3;
        int second = 4;
        var user = new User();
        user.setName("Job4j");
        var savedUser = userRepository.save(user);

        var calcEvent = service.add(savedUser, first, second);

        var foundEvent = repository.findById(calcEvent.getId());

        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getUser().getName()).isEqualTo("Job4j");
        assertEquals(calcEvent.getId(), foundEvent.get().getId());
    }
}
