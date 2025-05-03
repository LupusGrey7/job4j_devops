import java.io.FileInputStream
import java.util.*

plugins {
    checkstyle
    java
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotbugs) //Подключение SpotBugs для статического анализа кода
    alias(libs.plugins.liquibase)
    alias(libs.plugins.dotenv) // Подключаем плагин dotenv - для работы с переменными окружения
}

group = "ru.job4j.devops"
version = "1.0.0"

// 1. Определяем активное окружение // Устанавливаем переменную окружения, если она не задана явно
val activeEnv = System.getenv("ENV") ?: "local"

// 2. Файлы env в порядке приоритета
val envFiles = listOf(
    file("env/.env.$activeEnv"),    // основной файл для окружения
    file(".env.example")           // fallback-шаблон
)

// 3. Выбираем первый существующий файл
val envFile = envFiles.firstOrNull { it.exists() }
    ?: throw GradleException("No .env files found! Tried: ${envFiles.map { it.name }}")

// 4. Читаем .env файл (правильный способ)
val envProperties = Properties().apply {
    FileInputStream(envFile).use { fis ->
        load(fis)
    }
}

// объект для хранения исходного интеграционных тестов.
val integrationTest by sourceSets.creating {
    java {
        srcDir("src/integrationTest/java") // Убрать подпапку ru.job4j.develop.integration
    }
    resources {
        srcDir("src/integrationTest/resources")
    }
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}

// зависимости для тестирования
val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations["testImplementation"])
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations["testRuntimeOnly"])
}

//Вариант с кастомным конфигом (более правильно) // В разделе checkstyle
tasks.withType<Checkstyle>().configureEach {
    when (name) {
        "checkstyleIntegrationTest" -> try {
            val configFile = file("config/checkstyle/checkstyle-integration.xml").takeIf { it.exists() }
                ?: file("config/checkstyle/checkstyle.xml")

            config = resources.text.fromFile(configFile)
            maxErrors = 0
            maxWarnings = 10
        } catch (e: Exception) {
            logger.warn("Failed to configure checkstyle: ${e.message}")
            enabled = false
        }
    }
}

// 3. Улучшенный task с проверкой существования файлов // Копируем нужный .env файл из папки env в корень проекта
tasks.register<Copy>("prepareDotEnv") {
    group = "setup"
    description = "Prepare .env file for current environment"

    // Берем первый существующий файл из списка
    val sourceFile = envFiles.firstOrNull { it.exists() }
        ?: throw GradleException("No .env files found! Tried: ${envFiles.map { it.name }}")

    from(sourceFile)
    into(project.layout.projectDirectory)
    rename { ".env" }

    outputs.file(layout.projectDirectory.file(".env")) // Явно объявляем выходной файл

    doLast {
        logger.lifecycle("Using env file: ${sourceFile.name}")
    }
}

// 4. Проверяем перед сборкой
tasks.named("processResources") {
    dependsOn("prepareDotEnv")
    dependsOn("validateEnv")
}

repositories {
    mavenCentral()
}

dependencies {
    //Core
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)

    // Liquibase
    implementation(libs.liquibase.core)
    implementation(libs.postgresql)
    add("liquibaseRuntime", libs.liquibase.core)
    add("liquibaseRuntime", libs.postgresql)
    add("liquibaseRuntime", libs.jaxb.api)
    add("liquibaseRuntime", libs.logback.core)
    add("liquibaseRuntime", libs.logback.classic)
    add("liquibaseRuntime", libs.picocli)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Тестовые зависимости
    testImplementation(libs.spring.boot.starter.test) // Включает JUnit и AssertJ
    testImplementation(libs.assertj.core)             // Явное указание (если нужно)
    testImplementation(libs.h2)                        // Включает H2 для тестов
    testImplementation(libs.testcontainers.postgresql) // зависимости для Testcontainers.
}

// Liquibase конфигурация с переменными из .env
// Liquibase runtime dependencies (настроим профиль для Liquibase)+ добавили ENV из файла .env.example для локального окружения (пример "DB_USERNAME")
liquibase {
    activities.register("main") {
        val dbUrl = envProperties.getProperty("DB_URL") ?: "jdbc:h2:mem:testdb"
        val dbUser = envProperties.getProperty("DB_USERNAME") ?: "sa"
        val dbPass = envProperties.getProperty("DB_PASSWORD") ?: ""

        this.arguments = mapOf(
            "logLevel" to "info",
            "driver" to "org.postgresql.Driver",
            "url" to dbUrl,
            "username" to dbUser,
            "password" to dbPass,
            "classpath" to "src/main/resources",
            "changelogFile" to "db/changelog/db.changelog-master.xml"
        )
    }
    runList = "main"
    jvmArgs = "-Duser.dir=$projectDir"
}

//buildscript -  Liquibase
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.liquibase:liquibase-core:4.30.0")
    }
}

// Проверка покрытия
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.8".toBigDecimal()
            }
        }

        rule {
            isEnabled = false
            element = "CLASS"
            includes = listOf("org.gradle.*")

            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = "0.3".toBigDecimal()
            }
        }
    }
}

// 5. Настраиваем зависимости для всех задач компиляции
tasks.withType<AbstractCompile>().configureEach {
    dependsOn("prepareDotEnv") // Явная зависимость
}

// Эта конфигурация применяется ко всем задачам типа Test(включая test, integrationTest и другие)
tasks.withType<Test> {
//    useJUnitPlatform()
//    outputs.cacheIf { true } // Включаем кеширование для тестов

    // Устанавливаем системные свойства для подключения к БД
    systemProperty(
        "spring.datasource.url",
        envProperties.getProperty("DB_URL") ?: "jdbc:h2:mem:testdb"
    )
    systemProperty(
        "spring.datasource.username",
        envProperties.getProperty("DB_USERNAME") ?: "sa"
    )
    systemProperty(
        "spring.datasource.password",
        envProperties.getProperty("DB_PASSWORD") ?: ""
    )
}

// 6. Валидационная задача
tasks.register("validateEnv") {
    group = "Environment Management" // Group name for organizing related tasks
    description =
        "Validates the environment configuration by checking the .env file and logging key properties." // Brief description of what the task does

    dependsOn("prepareDotEnv") // Зависимость от prepareDotEnv
    inputs.file(layout.projectDirectory.file(".env")) // Явный input

    doLast {
        logger.lifecycle("✅ Using env file: ${envFile.path}")
        logger.lifecycle("   DB_URL=${envProperties.getProperty("DB_URL")}")
        logger.lifecycle("   DB_USER=${envProperties.getProperty("DB_USERNAME")}")
    }
}

// 7. Общая конфигурация
afterEvaluate {
    tasks.named("compileJava") {
        dependsOn("prepareDotEnv")
    }
    tasks.named("compileTestJava") {
        dependsOn("prepareDotEnv")
    }
    tasks.named("compileIntegrationTestJava") {
        dependsOn("prepareDotEnv")
    }
}

tasks.register<Zip>("zipJavaDoc") {
    group = "documentation" // Группа, в которой будет отображаться задача
    description = "Packs the generated Javadoc into a zip archive"

    dependsOn("javadoc") // Указываем, что задача зависит от выполнения javadoc

    from("build/docs/javadoc") // Исходная папка для упаковки
    archiveFileName.set("javadoc.zip") // Имя создаваемого архива
    destinationDirectory.set(layout.buildDirectory.dir("archives")) // Директория, куда будет сохранен архив
}

//SpotBugs для статического анализа кода
tasks.spotbugsMain {
    reports.create("html") {
        required = true
        outputLocation.set(layout.buildDirectory.file("reports/spotbugs/spotbugs.html"))
    }
}
// Эта конфигурация применяется только к конкретной задаче test
tasks.test {
    // Запускает spotbugsMain после завершения тестов
    finalizedBy(tasks.spotbugsMain)
}

//задача для проверки размера JAR-файла
tasks.register("checkJarSize") {
    group = "verification"
    description = "Checks the size of the generated JAR file."

    dependsOn("jar") // Задача зависит от сборки JAR

    doLast {
        val jarFile = file("build/libs/${project.name}-${project.version}.jar") // Путь к JAR-файлу
        if (jarFile.exists()) {
            val sizeInMB = jarFile.length() / (1024 * 1024) // Размер в мегабайтах
            if (sizeInMB > 5) {
                println("WARNING: JAR file exceeds the size limit of 5 MB. Current size: ${sizeInMB} MB")
            } else {
                println("JAR file is within the acceptable size limit. Current size: ${sizeInMB} MB")
            }
        } else {
            println("JAR file not found. Please make sure the build process completed successfully.")
        }
    }
}

// архивирует содержимое директории src/main/resources в ZIP-файл.
tasks.register<Zip>("archiveResources") {
    group = "custom optimization"
    description = "Archives the resources folder into a ZIP file"

    val inputDir = file("src/main/resources")
    // Используем layout.buildDirectory для корректного пути
    val outputDir = layout.buildDirectory.dir("archives")

    inputs.dir(inputDir) // Входные данные для инкрементальной сборки
    outputs.file(outputDir.map { it.file("resources.zip") }) // Выходной файл

    from(inputDir)
    destinationDirectory.set(outputDir)
    archiveFileName.set("resources.zip")

    doLast {
        println("Resources archived successfully at ${outputDir.get().asFile.absolutePath}")
    }
}

// Связываем задачу(архивирования содержимое директории) с жизненным циклом (например, после сборки JAR)
tasks.named("jar") {
    finalizedBy("archiveResources")
}

// Отключение проверки стиля для тестов
tasks.named("checkstyleTest") {
    enabled = false
}

//Проверка наличия плагина и его правильной работы. Вывести все переменные окружения
tasks.register("printEnvVariables") {
    group = "profile"
    description = "Prints all environment variables"

    doLast {
        System.getenv().forEach { (key, value) ->
            println("$key = $value")
        }
    }
}

// Печать переменных окружения
tasks.register("profile") {
    group = "profile"
    description = "Prints the active profile"

    doFirst {
        // Получаем профиль из gradle.properties или system properties
        val activeProfile = project.findProperty("spring.profiles.active")
            ?: project.property("springProfilesActive")
            ?: "default"
        println("Active profile: $activeProfile")

        // Доступ к переменным окружения (нужно установить их перед запуском Gradle)
        println(env.DB_URL.value)
        println(env.DB_USERNAME.value)
        println(env.DB_PASSWORD.value)

        //Также возможно Доступ к переменным из gradle.properties
        //println("DB_URL from gradle.properties: ${project.findProperty("db.url")}")
    }

    // группа check
    tasks.check {
        dependsOn("integrationTest")
    }

    // Task - задача для запуска интеграционных тестов
    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests"
        group = "verification"

        testClassesDirs = integrationTest.output.classesDirs
        classpath = integrationTest.runtimeClasspath

        useJUnitPlatform()
        shouldRunAfter(tasks.test)

        systemProperty(
            "spring.datasource.url",
            envProperties.getProperty("DB_URL") ?: "jdbc:h2:mem:testdb"
        )
    }
    // Для всех задач обработки ресурсов
    tasks.withType<ProcessResources>().configureEach {
        dependsOn("prepareDotEnv")
        mustRunAfter("prepareDotEnv")
    }

// Для тестовых ресурсов отдельно
    tasks.named("processTestResources") {
        dependsOn("prepareDotEnv")
        mustRunAfter("prepareDotEnv")
    }

// Для интеграционных тестов
    tasks.named("processIntegrationTestResources") {
        dependsOn("prepareDotEnv")
        mustRunAfter("prepareDotEnv")
    }
}
tasks.named<ProcessResources>("processTestResources") {
    dependsOn("prepareDotEnv")
}
