package ru.job4j.devops.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Видеть URL подключения при запуске
 */
@Component
public class EnvLogger implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvLogger.class);

    @Override
    public void run(ApplicationArguments args) {
        // Проверка уровня логирования перед вызовом
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("🔍 DB URL = {}", System.getenv("SPRING_DATASOURCE_URL"));
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("🔍 ACTIVE PROFILE = {}", System.getenv("SPRING_PROFILES_ACTIVE"));
        }
    }
}