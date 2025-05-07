package ru.job4j.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.job4j.devops.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);
}