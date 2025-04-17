FROM gradle:8.11.1-jdk21 AS builder
RUN mkdir job4j_devops
WORKDIR /job4j_devops
COPY . .
RUN gradle clean build -x test
RUN jar xf /job4j_devops/build/libs/DevOps-1.0.0.jar
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'BOOT-INF/lib/*' \
    /job4j_devops/build/libs/DevOps-1.0.0.jar > deps.info
RUN jlink \
    --add-modules $(cat deps.info) \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /slim-jre

# Собираем финальное образ
FROM debian:bookworm-slim
ENV JAVA_HOME /user/java/jdk21
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=builder /slim-jre $JAVA_HOME
COPY --from=builder /job4j_devops/build/libs/DevOps-1.0.0.jar .
ENTRYPOINT java -jar DevOps-1.0.0.jar

# FROM gradle:8.11.1-jdk21 -> Используем Gradle 8.11.1 и JDK 21
# RUN mkdir -> Создаёт директорию job4j_devops внутри контейнера
# RUN jar xf ... -> Распаковывает JAR-файл внутрь директории job4j_devops (в текущую рабочую директорию контейнера.)
# RUN jdeps ... ->  (Java Dependency Analysis Tool) анализирует зависимости классов и модулей в Java-приложениях. Она помогает понять, какие модули, пакеты или классы используются в JAR-файле, а также выявить лишние или устаревшие зависимости:
# RUN jlink ... -> Создает сжатую версию JRE, которая содержит только необходимые модули и библиотеки.
# WORKDIR ... -> Устанавливает рабочую директорию для последующих команд.
# COPY ... -> Копирует содержимое текущей директории(все файлы) внутрь контейнера
# RUN gradle... -> Запускает сборку проекта с помощью Gradle, пропуская тесты (-x test).
# EXPOSE ...-> Декларирует использование порта 8080 контейнером.
# ENTRYPOINT ... -> Указывает команду для запуска приложения (исполнение JAR-файла).
