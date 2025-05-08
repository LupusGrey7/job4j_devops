package ru.job4j.develop.integration.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.develop.integration.config.ContainersConfig;

import ru.job4j.devops.models.User;
import ru.job4j.devops.repository.CalcEventRepository;
import ru.job4j.devops.repository.UserRepository;
import ru.job4j.devops.service.CalcEventService;

@ActiveProfiles("integration")
public class CalcEventServiceIntegrationTest extends ContainersConfig {

    @Autowired
    CalcEventService eventService;

    @Autowired
    private CalcEventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenSaveUser() {
        int first = 3;
        int second = 4;
        var user = new User();
        user.setName("Job4j");

        var savedUser = userRepository.save(user);
        var calcEvent = eventService.add(savedUser, first, second);
        var foundEvent = eventRepository.findById(calcEvent.getId());

        assertThat(calcEvent).isNotNull();
        assertThat(calcEvent.getId()).isNotNull();
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getUser().getName()).isEqualTo("Job4j");
        Assertions.assertEquals(calcEvent.getId(), foundEvent.get().getId());
    }
}
