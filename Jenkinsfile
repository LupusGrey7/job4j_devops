pipeline {
    agent { label 'agent1' }

    // ➤➤➤ Добавляем блок environment для переменных кэша
    environment {
        // 1. Путь из контейнера agent1
        JAVA_HOME = '/opt/java/openjdk'

        // 2. Окружение и .env файл
        ENV = "${params.ENV ?: 'develop'}" // Делаем параметризуемым
        DOTENV_BASE_DIR = "/var/agent-jdk21/env" // Базовая директория
        DOTENV_FILE = "${DOTENV_BASE_DIR}/.env.${ENV}" // Полный путь

        // 3. Важные флаги (можно переопределять в параметрах pipeline)
        SKIP_TESTS = "${params.SKIP_TESTS ?: false}"

        // 4. Настройки кэша (как у вас)// Логин/пароль из хранилища секретов Jenkins (рекомендуемый способ)
//         GRADLE_REMOTE_CACHE_USERNAME = "${env.GRADLE_REMOTE_CACHE_USERNAME}"
//         GRADLE_REMOTE_CACHE_PASSWORD = "${env.GRADLE_REMOTE_CACHE_PASSWORD}"
        // 5.URL кэша из системных переменных Jenkins (если задан)
        GRADLE_REMOTE_CACHE_URL = "${env.GRADLE_REMOTE_CACHE_URL ?: 'http://192.168.0.109:5071/'}"  // Без /cache/
    }

    parameters {
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Пропустить выполнение тестов?'
        )
        choice(
            name: 'ENV',
            choices: ['develop', 'ci', 'stage'],
            description: 'Выберите окружение'
        )
    }

    tools {
        git 'Default'
       // jdk 'jdk-21' // Явно указываем JDK
    }

    stages {
        stage('Init') { //содержит этапы выполнения.
            steps { // шаги, выполняемые на данном этапе.
                script {
                    // Проверка существования скрипта
                    if (!fileExists('scripts/gradleUtils.groovy')) {
                        error "❌ gradleUtils.groovy not found!"
                    }
                    //В данном случае используется команда echo для вывода текста.
                    echo "🔄 Loading gradleUtils..."
                    // Сначала загружаем
                    runGradleTask = load 'scripts/gradleUtils.groovy'
                    sh 'chmod +x ./gradlew'

                    //затем проверяем
                    if (runGradleTask == null) {
                        error "❌ runGradleTask is NULL! Did you forget to commit scripts/gradleUtils.groovy?"
                    }

                    // Логирование информации о среде
                    echo """
                    ⚙️ Environment Info:
                    - ENV: ${ENV}
                    - Java: ${JAVA_HOME}
                    - Gradle Cache: ${GRADLE_REMOTE_CACHE_URL}
                    """
                }
            }
        }

        stage('Checkstyle') {
            steps {
                script {
                    runGradleTask('checkstyleMain checkstyleTest', 'Checkstyle FAILED', DOTENV_FILE)
                }
            }
        }

        stage('Compile') {
            steps {
                script {
                    runGradleTask('compileJava', 'Compilation FAILED', DOTENV_FILE)
                }
            }
        }

        stage('Test') {
            when {
                expression { return !params.SKIP_TESTS.toBoolean() }
            }
            steps {
                script {
                    runGradleTask('test', 'Tests FAILED', DOTENV_FILE)
                }
            }
        }

        stage('Code Coverage') {
            steps {
                script {
                    runGradleTask('jacocoTestReport jacocoTestCoverageVerification', 'Code coverage FAILED', DOTENV_FILE)
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    try {
                        // Унифицированный вызов через runGradleTask
                        runGradleTask(
                            "clean build --build-cache --refresh-dependencies -x test " +
                            "-Dorg.gradle.caching.remote.allow-insecure-protocol=true " + // Разрешить HTTP не требовать HTTPS
                            "-Pdotenv.filename=${DOTENV_FILE} " +
                            "-Dgradle.cache.remote.url=${GRADLE_REMOTE_CACHE_URL}",
//                             "-Dgradle.cache.remote.username=${GRADLE_REMOTE_CACHE_USERNAME} " +
//                             "-Dgradle.cache.remote.password=${GRADLE_REMOTE_CACHE_PASSWORD}",
                            'Build FAILED',
                            DOTENV_FILE
                        )
                        telegramSend(message: "✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                    } catch (e) {
                        telegramSend(message: "❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                        error "Build failed: ${e.message}"
                    }
                }
            }
        }

        stage('Update DB') {
            steps {
                script {
                    runGradleTask('update', 'Update DB FAILED', DOTENV_FILE)
                }
            }
        }
    }

    // Добавляем блок post для отправки уведомлений в Telegram
    post {
        always {
            script {
                def buildInfo = """
                📊 Build Info:
                Job: ${env.JOB_NAME}
                Build #: ${currentBuild.number}
                Status: ${currentBuild.currentResult}
                Duration: ${currentBuild.durationString}
                Environment: ${ENV}
                """.stripIndent()

                telegramSend(message: buildInfo)

                // Очистка .env файла если он был скопирован
                if (fileExists('.env')) {
                    sh 'rm -f .env'
                }
            }
        }

        success {
            echo "Build succeeded!"
        }

        failure {
            echo "Build failed!"
        }

        unstable {
            echo "Build unstable!"
            telegramSend(message: "⚠️ Build UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
    }
}

// шаблон
//pipeline {
//    agent { label 'agent1' }
//    environment { ... }
//    tools { ... }
//    stages { ... }
//    post { ... }
//}

//stage('Build') {
//               steps {
//                   script {
//                       try {
//                           // Унифицированный вызов через runGradleTask
//                           runGradleTask(
//                               "clean build --build-cache --refresh-dependencies -x test " +
//                               "-Dorg.gradle.caching.remote.allow-insecure-protocol=true " + // Разрешить HTTP не требовать HTTPS
//                               "-Pdotenv.filename=${DOTENV_FILE} " +
//                               "-Dgradle.cache.remote.url=${GRADLE_REMOTE_CACHE_URL} " +
//                               "-Dgradle.cache.remote.username=${GRADLE_REMOTE_CACHE_USERNAME} " +
//                               "-Dgradle.cache.remote.password=${GRADLE_REMOTE_CACHE_PASSWORD}",
//                               'Build FAILED',
//                               DOTENV_FILE
//                           )
//                           telegramSend(message: "✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
//                       } catch (e) {
//                           telegramSend(message: "❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
//                           error "Build failed: ${e.message}"
//                       }
//                   }
//               }
//           }