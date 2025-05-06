package ru.job4j.devops;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

/*
  в тесте по необходимости можно
  1. Устанавливаем и удаляем системные свойства перед вызовом main()
  System.setProperty("spring.profiles.active", "test");
  System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
  System.setProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
  System.clearProperty("spring.profiles.active");
  а можно задать через файл сборки gradle.build.kt - tasks.test {}
  */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CalcApplicationMainTest {

    Logger logger = LoggerFactory.getLogger(CalcApplicationMainTest.class);

    @Autowired
    private Environment env;

    @Test
    void printProperties() {
        System.out.println("Datasource URL: " + env.getProperty("spring.datasource.url"));
        System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
        assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:h2:mem:testdb");
    }

    @Test
    void mainMethodTest() {
        logger.info("Test profile is active: {}", System.getProperty("spring.profiles.active"));

        // 2. Вызываем main
        CalcApplication.main(new String[]{});

        // 3. Проверяем
        assertThat(System.getProperty("spring.profiles.active")).isEqualTo("test");
        Assertions.assertNotNull(System.getProperty("spring.profiles.active"));
        Assertions.assertEquals("test", System.getProperty("spring.profiles.active"));
    }

}
