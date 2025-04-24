plugins {
    checkstyle
    java
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotbugs) //Подключение SpotBugs для статического анализа кода
    alias(libs.plugins.liquibase)
}

group = "ru.job4j.devops"
version = "1.0.0"

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
    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Тестовые зависимости
    testImplementation(libs.spring.boot.starter.test) // Включает JUnit и AssertJ
    testImplementation(libs.assertj.core)             // Явное указание (если нужно)
    testImplementation(libs.h2)                        // Включает H2 для тестов

}

// Liquibase
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.liquibase:liquibase-core:4.30.0")
    }
}

// Liquibase runtime dependencies (настроим профиль для Liquibase)
liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "logLevel" to "info",
            "url" to "jdbc:postgresql://localhost:5432/job4j_devops",
            "username" to "postgres",
            "password" to "password",
            "classpath" to "src/main/resources",
            "changelogFile" to "db/changelog/db.changelog-master.xml"
        )
    }
    runList = "main"
}

dependencies {
    add("liquibaseRuntime", libs.liquibase.core)
    add("liquibaseRuntime", libs.postgresql)
    add("liquibaseRuntime", libs.jaxb.api)
    add("liquibaseRuntime", libs.logback.core)
    add("liquibaseRuntime", libs.logback.classic)
    add("liquibaseRuntime", libs.picocli)

}

tasks.withType<Test> {
    useJUnitPlatform()
    outputs.cacheIf { true } // добавим gradle кеширование для задачи
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

tasks.test {
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

// отключаем проверку стиля
tasks.named("checkstyleTest") {
    enabled = false
}