package ru.job4j.develop.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.develop.integration.config.ContainersConfig;
import ru.job4j.devops.models.User;
import ru.job4j.devops.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration")
class UserRepositoryTest extends ContainersConfig {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenSaveUser() {
        var user = new User();
        user.setName("Job4j");

        userRepository.save(user);
        var foundUser = userRepository.findById(user.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Job4j");
    }
}
