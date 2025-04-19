# Этап сборки
FROM gradle:8.11.1-jdk21 AS builder

WORKDIR /job4j_devops

# 1. Копируем файлы конфигурации Gradle
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

#  Отключаем проблемный remote build cache
#RUN sed -i '/remote<HttpBuildCache>/d' settings.gradle.kts

# 2. Скачиваем зависимости
RUN gradle --no-daemon dependencies

# 3. Копируем исходный код
COPY src ./src

# 4. Собираем проект
RUN gradle --no-daemon build -x test

# 5. Проверяем наличие JAR-файла
RUN ls -l /app/build/libs/

# 6. Анализ зависимостей для jlink (исправленный синтаксис)
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'BOOT-INF/lib/*' \
    /job4j_devops/build/libs/DevOps-1.0.0.jar > deps.info

# 7. Создаем slim JRE (+ обязательные модули)
RUN jlink \
    --add-modules $(cat deps.info),jdk.crypto.ec,java.sql,java.management \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /slim-jre

# Финальный образ
FROM debian:bookworm-slim

# 8. Настройка переменных среды
ENV JAVA_HOME=/opt/slim-jre
ENV PATH="$JAVA_HOME/bin:$PATH"

# 9. Копируем необходимые файлы- JRE и приложение
COPY --from=builder /slim-jre $JAVA_HOME
COPY --from=builder /job4j_devops/build/libs/DevOps-1.0.0.jar /job4j_devops/app.jar

# 10. Настройки для запуска
WORKDIR /job4j_devops
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "DevOps-1.0.0.jar"]


# Этап сборки
# FROM gradle:8.11.1-jdk21 -> Используем Gradle 8.11.1 и JDK 21
# RUN mkdir -> Создаёт директорию job4j_devops внутри контейнера
# WORKDIR ... -> Устанавливает рабочую директорию для последующих команд.

# Копируем файлы для зависимостей, включая папку gradle
# COPY ... -> Копирует все содержимое текущей директории внутрь директории job4j_devops.
# RUN gradle... -> Запускает сборку проекта с помощью Gradle, пропуская тесты (-x test).
# RUN jar xf ... -> Распаковывает JAR-файл внутрь директории job4j_devops (в текущую рабочую директорию контейнера.)
# RUN jdeps ... ->  (Java Dependency Analysis Tool) анализирует зависимости классов и модулей в Java-приложениях. Она помогает понять, какие модули, пакеты или классы используются в JAR-файле, а также выявить лишние или устаревшие зависимости:
# RUN jlink ... -> Создает сжатую версию JRE, которая содержит только необходимые модули и библиотеки.
        #--add-modules $(cat deps.info): Добавляет модули, указанные в файле deps.info.
         #--strip-debug: Удаляет отладочную информацию из JRE, чтобы уменьшить его размер.
         #--compress 2: Сжимает классы в JRE с максимальной степенью (ZIP).
         #--no-header-files: Исключает файлы заголовков из JRE.
         #--no-man-pages: Исключает страницы справки (man pages).
         #--output /slim-jre: Указывает директорию для вывода минимального JRE.

# Собираем финальное образ --->
# FROM  debian:bookworm-slim -> Используем базовый образ Debian 11 (bookworm) с установленным slim-jre

# Установка переменных среды в правильном формате
# ENV JAVA_HOME /user/java/jdk21 -> Устанавливает переменную окружения JAVA_HOME в /user/java/jdk21
# ENV PATH $JAVA_HOME/bin:$PATH -> Добавляет путь к директории с JRE в переменную окружения PATH

# Копируем исходный код и собираем проект
# COPY --from=builder /slim-jre -> Копирует сжатую версию JRE из предыдущего этапа внутрь контейнера.
        #/slim-jre -> Путь к сжатой версии JRE в предыдущем этапе.
#COPY --from=builder /job4j_devops/build/libs/DevOps-1.0.0.jar -> Копирует JAR-файл DevOps-1.0.0.jar из предыдущего этапа внутрь контейнера.

# COPY ... -> Копирует содержимое текущей директории(все файлы) внутрь контейнера
# EXPOSE ...-> Декларирует использование порта 8080 контейнером.
# ENTRYPOINT ... -> Указывает команду для запуска приложения (исполнение JAR-файла).
