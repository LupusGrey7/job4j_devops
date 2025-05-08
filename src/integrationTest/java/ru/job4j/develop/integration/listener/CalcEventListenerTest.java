package ru.job4j.develop.integration.listener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.develop.integration.config.ContainersConfig;

import ru.job4j.devops.models.User;
import ru.job4j.devops.repository.CalcEventRepository;
import ru.job4j.devops.repository.UserRepository;
import ru.job4j.devops.service.CalcEventService;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("integration")
public class CalcEventListenerTest extends ContainersConfig {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    CalcEventService eventService;

    @Autowired
    private CalcEventRepository eventRepository;

    @Test
    void whenCreatedNewCalcEvent() {
        int first = 3;
        int second = 4;
        var user = new User();
        user.setName("Job4j new member : " + System.nanoTime());

        var savedUser = userRepository.save(user);
        var calcEvent = eventService.add(savedUser, first, second);

        kafkaTemplate.send("event", calcEvent);

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(10, SECONDS)
                .untilAsserted(() -> {
                    var optionalUser = eventRepository.findById(calcEvent.getId());
                    assertThat(optionalUser).isPresent();
                });
    }
}
