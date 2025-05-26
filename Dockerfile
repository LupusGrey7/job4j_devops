# Image Dockerfile без привязки к ENV
# 🔨 Этап сборки: используем Gradle с JDK 21
FROM gradle:8.11.1-jdk21 AS builder

# Создаём рабочую директорию
WORKDIR /job4j_devops

# 1. Копируем только необходимые для скачивания зависимостей файлы
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
COPY build.gradle.kts gradle.properties settings.gradle.kts ./

# 2. Копируем исходники и конфиги
COPY src ./src
COPY config/checkstyle/checkstyle.xml ./config/checkstyle/checkstyle.xml

# 3. Отключаем remote cache временно (если используется)
RUN if [ -f settings.gradle.kts ]; then sed -i '/remote(HttpBuildCache::class)/,/}/d' settings.gradle.kts; fi

# 4. Скачиваем зависимости (чтобы использовать слои докера)
RUN gradle --no-daemon dependencies

# 5. Собираем проект без тестов и без checkstyle
RUN gradle --no-daemon build -x test -x checkstyleMain -x checkstyleTest -x integrationTest

# 6. Проверка, что JAR-файл создан
RUN ls -l build/libs/

# 7. Анализ зависимостей JAR (для jlink)
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'BOOT-INF/lib/*' \
    build/libs/DevOps-1.0.0.jar > deps.info

# 8. Создаём slim JRE (с нужными модулями)
RUN jlink \
    --add-modules $(cat deps.info),jdk.crypto.ec,java.instrument,java.security.jgss,java.sql,java.management,java.naming,java.desktop,jdk.unsupported \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /slim-jre

# ✅ Финальный образ — минимальный и быстрый
FROM debian:bookworm-slim

# 9. Устанавливаем JAVA_HOME и добавляем его в PATH
ENV JAVA_HOME=/opt/slim-jre
ENV PATH="$JAVA_HOME/bin:$PATH"

# 10. Копируем JRE и JAR из builder-слоя
COPY --from=builder /slim-jre $JAVA_HOME
COPY --from=builder /job4j_devops/build/libs/DevOps-1.0.0.jar /job4j_devops/DevOps-1.0.0.jar

# 11. Настройки запуска
WORKDIR /job4j_devops
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "DevOps-1.0.0.jar"]



## рабочий образ - но в нем жестко задана ENV среда и переменные окружения
## Этап сборки
#FROM gradle:8.11.1-jdk21 AS builder
#RUN mkdir job4j_devops
#WORKDIR /job4j_devops
#
## 1. Копируем Gradle wrapper и конфиги
#COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
#COPY build.gradle.kts gradle.properties ./
#COPY settings.gradle.kts ./settings.gradle.kts
## 🆕 Добавим это, если у тебя есть version catalog
#COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
#
## 🆕 2. Копируем исходники и конфиги( buildSrc - чаще всего используется для кастомных плагинов)
#COPY src ./src
#COPY config/checkstyle/checkstyle.xml ./config/checkstyle/checkstyle.xml
#
## 3. Копируем buildSrc (если есть кастомные плагины в Градл и папка в корне проекта называется buildSrc)
##COPY buildSrc ./buildSrc
#
## Копируем конфигурацию Checkstyle, если она есть
#COPY config/checkstyle/checkstyle.xml ./config/checkstyle/checkstyle.xml
#
## 4. Копируем .env-файлы 👈
#COPY env/.env.local ./env/.env.local
#COPY env/.env.local ./.env
#COPY env/.env.develop ./env/.env.develop
#COPY .env.example ./.env.example
#
## 5. Устанавливаем переменную окружения
#ENV ENV=local
#
## 6. Временно отключаем remote cache в settings.gradle.kts (если есть)
#RUN if [ -f settings.gradle.kts ]; then sed -i '/remote(HttpBuildCache::class)/,/}/d' settings.gradle.kts; fi
## 6.1. Временно отключаем checkstyle и remote cache
##RUN sed -i '/checkstyleMain/d' build.gradle.kts && \
# #   if [ -f settings.gradle.kts ]; then sed -i '/remote(HttpBuildCache::class)/,/}/d' settings.gradle.kts; fi
#
## 7. Скачиваем зависимости
#RUN gradle --no-daemon dependencies
#
## 8. Собираем проект (отключаем при сборке в докер образ тесты и checkstyle\Интеграционные тесты)
#RUN gradle --no-daemon build -x test -x checkstyleMain -x checkstyleTest -x integrationTest
#
## 9. Проверяем результат - Проверяем наличие JAR-файла
#RUN ls -l /job4j_devops/build/libs/
#
## 10. Анализ зависимостей
#RUN jdeps --ignore-missing-deps -q \
#    --recursive \
#    --multi-release 21 \
#    --print-module-deps \
#    --class-path 'BOOT-INF/lib/*' \
#    /job4j_devops/build/libs/DevOps-1.0.0.jar > deps.info
#
## 10.1. Отладка: выводим содержимое deps.info
#RUN cat deps.info
#
## 11. Создаем slim JRE с дополнительными модулями
#RUN jlink \
#    --add-modules $(cat deps.info),jdk.crypto.ec,java.instrument,java.security.jgss,java.sql,java.management,java.naming,java.desktop,jdk.unsupported \
#    --strip-debug \
#    --compress 2 \
#    --no-header-files \
#    --no-man-pages \
#    --output /slim-jre
#
## 11.1. Проверяем наличие slim JRE
#RUN ls -l /slim-jre/bin/
#
## 11.2. Проверяем наличие JAR-файла
#RUN ls -l /job4j_devops/build/libs/
#
## Финальный образ
#FROM debian:bookworm-slim
#
## 12. Установка переменных среды
#ENV JAVA_HOME=/opt/slim-jre
#ENV PATH="$JAVA_HOME/bin:$PATH"
#
## 13. Копируем JRE и приложение
#COPY --from=builder /slim-jre $JAVA_HOME
#COPY --from=builder /job4j_devops/build/libs/DevOps-1.0.0.jar /job4j_devops/DevOps-1.0.0.jar
#
## 14. Настройки для запуска
#WORKDIR /job4j_devops
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "DevOps-1.0.0.jar"]


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
