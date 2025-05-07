package ru.job4j.devops.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.job4j.devops.models.User;
import ru.job4j.devops.repository.UserRepository;

@Component
@AllArgsConstructor
@Slf4j
public class UserSignUpEventListener {

    private final UserRepository userRepository;

    @KafkaListener(topics = "signup", groupId = "job4j")
    public void signup(User user, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.debug("Received user from topic {}: {}", topic, user.getName());
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            throw e;
        }
    }
}
