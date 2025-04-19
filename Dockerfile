# Этап сборки
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

FROM debian:bookworm-slim
ENV JAVA_HOME /user/java/jdk21
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=builder /slim-jre $JAVA_HOME
COPY --from=builder /job4j_devops/build/libs/DevOps-1.0.0.jar .
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
