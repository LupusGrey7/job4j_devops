package ru.job4j.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.job4j.devops.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
}