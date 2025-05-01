plugins {
    checkstyle
    java
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotbugs) //Подключение SpotBugs для статического анализа кода
    alias(libs.plugins.liquibase)
    alias(libs.plugins.dotenv) // Подключаем плагин dotenv - для работы с переменными окружения
    //  id("co.uzzu.dotenv.gradle") version "4.0.0"
}

group = "ru.job4j.devops"
version = "1.0.0"

// Устанавливаем переменную окружения, если она не задана явно
val activeEnv = System.getenv("ENV") ?: "local"
val envFile = file("env/.env.$activeEnv")
//val dotEnvTarget = file(".env")

// Копируем нужный .env файл из папки env в корень проекта
tasks.register<Copy>("prepareDotEnv") {
    group = "prepareDotEnv"
    description = "Копируем нужный .env файл из папки env в корень проекта"

    from(envFile)
    into(layout.buildDirectory.dir("dotEnv"))
    rename { ".env" }
    doNotTrackState("We want this task to skip tracking file state for Gradle 8+ compatibility.")
}

tasks.named("processResources") {
    dependsOn("prepareDotEnv")
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
}

// Liquibase конфигурация с переменными из .env
// Liquibase runtime dependencies (настроим профиль для Liquibase)+ добавили ENV из файла .env.local для локального окружения (пример "DB_USERNAME")
liquibase {
      activities.register("main") {
        val dbUrl = env.DB_URL.value
        val dbUsername = env.DB_USERNAME.value
        val dbPassword = env.DB_PASSWORD.value

        this.arguments = mapOf(
            "logLevel" to "info",
            "driver" to "org.postgresql.Driver",
            "url" to dbUrl,
            "username" to dbUsername,
            "password" to dbPassword,
            "classpath" to "${project.rootDir}/application/src/main/",
            "searchPath" to "${project.rootDir}/application/src/main/resources/",
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

// Эта конфигурация применяется ко всем задачам типа Test(включая test, integrationTest и другие)
tasks.withType<Test> {
//    useJUnitPlatform()
//    outputs.cacheIf { true } // Включаем кеширование для тестов

    // Устанавливаем системные свойства для подключения к БД
    systemProperty("spring.datasource.url", env.DB_URL.value)
    systemProperty("spring.datasource.username", env.DB_USERNAME.value)
    systemProperty("spring.datasource.password", env.DB_PASSWORD.value)
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
}
