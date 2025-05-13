import java.io.FileInputStream
import java.util.*

// --- Plugins ---
plugins {
    checkstyle
    java
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotbugs) //Подключение SpotBugs для статического анализа кода
    alias(libs.plugins.liquibase.gradle) // Подключаем плагин Liquibase для работы с миграциями базы данных
    alias(libs.plugins.dotenv) // Подключаем плагин dotenv - для работы с переменными окружения
}

group = "ru.job4j.devops"
version = "1.0.0"

// --- Конфигурация зависимостей Gradle для плагина Liquibase ---

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.liquibase.core)
    }
}

// --- Работа с переменными окружения (.env) ---

val activeEnv = System.getenv("ENV")
    ?: "local" // 1. Определяем активное окружение // Устанавливаем переменную окружения, если она не задана явно
val envFiles = listOf(
    file("env/.env.$activeEnv"), // основной файл для окружения
    file(".env.example") // fallback-шаблон
)

val envProperties = Properties().apply {
    val envFile = envFiles.firstOrNull { it.exists() } // Выбираем первый существующий файл

    if (envFile != null) { // Читаем .env файл (правильный способ)
        FileInputStream(envFile).use { fis ->
            load(fis)
        }
        logger.lifecycle("✅ Loaded .env file: ${envFile.name}")
    } else {
        logger.warn("⚠️ No .env files found. Tried: ${envFiles.map { it.name }}. Using default values.")
    }
}

tasks.register("validateEnv") {
    group = "Environment Management"
    description = "Validates the environment configuration"
    inputs.files(envFiles.filter { it.exists() })
    doLast {
        if (envProperties.isEmpty) {
            logger.warn("No .env file loaded. Using default database settings.")
        } else {
            logger.lifecycle("✅ Environment configuration:")
            logger.lifecycle("   DB_URL=${envProperties.getProperty("DB_URL") ?: "default"}")
            logger.lifecycle("   DB_USER=${envProperties.getProperty("DB_USERNAME") ?: "default"}")
        }
    }
}

// ----- Конфигурация sourceSets для интеграционных тестов ----- // -отключим для CI\CD Jenkins Server
val integrationTest by sourceSets.creating {
    java {
        srcDir("src/integrationTest/java")
    }
    resources {
        srcDir("src/integrationTest/resources")
    }
    compileClasspath += sourceSets["main"].output + sourceSets["test"].output
    runtimeClasspath += output + compileClasspath
}

// ----- Конфигурация зависимостей для интеграционных тестов ----- //
val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations["testImplementation"])
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations["testRuntimeOnly"])
}

// --- Checkstyle ---
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

tasks.named("checkstyleTest") {
    enabled = false
}

// --- Репозитории и зависимости ---

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

dependencies {
    // Core
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)

    // Liquibase
    implementation(libs.liquibase.core)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.spring.kafka)
    add("liquibaseRuntime", libs.liquibase.core)
    add("liquibaseRuntime", libs.postgresql)
    add("liquibaseRuntime", libs.h2)
    add("liquibaseRuntime", libs.jaxb.api)
    add("liquibaseRuntime", libs.logback.core)
    add("liquibaseRuntime", libs.logback.classic)
    add("liquibaseRuntime", libs.picocli)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Тестовые зависимости
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.h2)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.awaitility)
    // Testcontainers core + JUnit 5 support (обязательно для аннотаций @Testcontainers, @Container)
    testImplementation(libs.testcontainers.junit.jupiter)

    // Для main sourceSet
    compileOnly(libs.spotbugs.annotations)

    // Для тестов (если нужно)
    testCompileOnly(libs.spotbugs.annotations)

    // Интеграционные тесты
    integrationTestImplementation(libs.liquibase.core)
    integrationTestImplementation(libs.postgresql)
    integrationTestImplementation(libs.h2)
    integrationTestImplementation(libs.testcontainers.testcontainers)
    integrationTestImplementation(libs.testcontainers.postgresql)
    integrationTestImplementation(libs.testcontainers.kafka)
}

// --- Liquibase ---
// Liquibase runtime dependencies (настроим профиль для Liquibase)+ добавили ENV из файла .env.example для локального окружения (пример "DB_USERNAME")
liquibase {
    activities.register("main") {
        val dbUrl = envProperties.getProperty("DB_URL") ?: "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        val dbUser = envProperties.getProperty("DB_USERNAME") ?: "sa"
        val dbPass = envProperties.getProperty("DB_PASSWORD") ?: ""

        arguments = mapOf(
            "logLevel" to "info",
            "driver" to if (dbUrl.contains("h2")) "org.h2.Driver" else "org.postgresql.Driver",
            "url" to dbUrl,
            "username" to dbUser,
            "password" to dbPass,
            "classpath" to "src/main/resources",
            "changelogFile" to "db/changelog/db.changelog-master.xml"
        )
    }
    runList = "main"
}

// --- Jacoco ---

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.2".toBigDecimal() //рекомендуется использовать 0.7 или 0.8 в бизнес-проектах
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

// --- Тесты ---

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // Для тестов с профилем "test" используем H2 в памяти
    if (project.findProperty("spring.profiles.active")?.toString()?.contains("test") == true) {
        systemProperty("spring.datasource.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
        systemProperty("spring.datasource.driver-class-name", "org.h2.Driver")
        systemProperty("spring.datasource.username", "sa")
        systemProperty("spring.datasource.password", "")
    } else {
        systemProperty(
            "spring.datasource.url",
            envProperties.getProperty("DB_URL") ?: "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        )
        systemProperty(
            "spring.datasource.driver-class-name",
            if (envProperties.getProperty("DB_URL")
                    ?.contains("h2") == true
            ) "org.h2.Driver" else "org.postgresql.Driver"
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
}

//отдельную задачу для запуска интеграционных тестов
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath

    useJUnitPlatform() // Используем JUnit 5
    systemProperty("spring.profiles.active", "integration") // Подключаем нужный профиль

    // Запускать только если переменная CI отсутствует
    onlyIf {
        !System.getenv().containsKey("CI")
    }

    shouldRunAfter(tasks.named("test")) // Убедимся, что запускается после обычных тестов
}
tasks.check {
    dependsOn("integrationTest")
}

// --- SpotBugs ---
//SpotBugs для статического анализа кода
tasks.spotbugsMain {
    reports.create("html") {
        required = true
        outputLocation.set(layout.buildDirectory.file("reports/spotbugs/spotbugs.html"))
    }
}

tasks.test {
    finalizedBy(tasks.spotbugsMain)
    useJUnitPlatform()
    maxParallelForks = 1 // ⛔️ Один поток
}

// --- Прочие задачи ---

tasks.register<Zip>("zipJavaDoc") {
    group = "documentation" // Группа, в которой будет отображаться задача
    description = "Packs the generated Javadoc into a zip archive"
    dependsOn("javadoc")
    from("build/docs/javadoc")
    archiveFileName.set("javadoc.zip")
    destinationDirectory.set(layout.buildDirectory.dir("archives"))
}

//задача для проверки размера JAR-файла
tasks.register("checkJarSize") {
    group = "verification"
    description = "Checks the size of the generated JAR file"
    dependsOn("jar") // Задача зависит от сборки JAR
    doLast {
        val jarFile = file("build/libs/${project.name}-${project.version}.jar")
        if (jarFile.exists()) {
            val sizeInMB = jarFile.length() / (1024 * 1024)
            if (sizeInMB > 5) {
                logger.warn("WARNING: JAR file exceeds 5 MB. Size: $sizeInMB MB")
            } else {
                logger.lifecycle("JAR file size: $sizeInMB MB")
            }
        } else {
            logger.error("JAR file not found")
        }
    }
}

// архивирует содержимое директории src/main/resources в ZIP-файл.
tasks.register<Zip>("archiveResources") {
    group = "custom optimization"
    description = "Archives the resources folder into a ZIP file"

    val inputDir = file("src/main/resources")
    val outputDir =
        layout.buildDirectory.dir("archives") // Связываем задачу(архивирования содержимое директории) с жизненным циклом (например, после сборки JAR)

    inputs.dir(inputDir)
    outputs.file(outputDir.map { it.file("resources.zip") })
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

// --- Переменные окружения ---

tasks.register("printEnvVariables") {
    group = "profile"
    description = "Prints all environment variables"
    doLast {
        System.getenv().forEach { (key, value) ->
            logger.lifecycle("$key = $value")
        }
    }
}

// Печать переменных окружения
tasks.register("profile") {
    group = "profile"
    description = "Prints the active profile"

    doLast {
        // Получаем профиль из gradle.properties или system properties
        val activeProfile = project.findProperty("spring.profiles.active")
            ?: project.property("springProfilesActive") ?: "default"

        logger.lifecycle("Active profile: $activeProfile")
        logger.lifecycle("DB_URL: ${envProperties.getProperty("DB_URL") ?: "default"}")
        logger.lifecycle("DB_USERNAME: ${envProperties.getProperty("DB_USERNAME") ?: "default"}")
        logger.lifecycle("DB_PASSWORD: ${envProperties.getProperty("DB_PASSWORD") ?: "default"}")
    }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test") // 🟢 активируем профиль test
    systemProperty("ENV", "test") // Для dotenv плагина
    systemProperty("spring.datasource.url", "jdbc:h2:mem:testdb")
    systemProperty("spring.datasource.driver-class-name", "org.h2.Driver")
}

// -----Integration Test Task ----//
tasks.named<Test>("integrationTest") {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "integration")
    onlyIf {
        // Запускаем только вне CI, например, если нет переменной окружения
        !System.getenv().containsKey("CI")
    }
}

tasks.named<ProcessResources>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// ----------------------------------------------
// Логгируем активные переменные среды при сборке
// ----------------------------------------------
gradle.taskGraph.whenReady {
    val env = System.getenv("ENV") ?: "local" // или "develop" по умолчанию, если нужно

    val envFile = file("env/.env.$env").takeIf { it.exists() }
        ?: file("env/.env.example").takeIf { it.exists() }

    val props = Properties()

    if (envFile != null) {
        println("✅ Load .env-file: ${envFile.name}")
        envFile.inputStream().use { props.load(it) }
    } else {
        println("⚠️ .env-file not founded (ENV=$env)")
    }

    val springProfile = props.getProperty("SPRING_PROFILES_ACTIVE") ?: "not installed" //не установлен
    val datasourceUrl = props.getProperty("SPRING_DATASOURCE_URL") ?: "not specified" // не указан
    val dbType = when {
        "h2" in datasourceUrl.lowercase() -> "H2"
        "postgres" in datasourceUrl.lowercase() -> "PostgreSQL"
        datasourceUrl != "не указан" -> "Unknown"
        else -> "Datasource not set"
    }

    println()
    println("===== Build Environment Information =====")
    println("Active Profile        : $springProfile")
    println("Datasource URL        : $datasourceUrl")
    println("DB Type               : $dbType")
    println("========================================")
    println()
}

