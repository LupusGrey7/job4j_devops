pipeline {
    agent { label 'agent1' }

// ➤➤➤ Добавляем блок environment для переменных кэша
    environment {
// Логин/пароль из хранилища секретов Jenkins (рекомендуемый способ)
        GRADLE_REMOTE_CACHE_USERNAME = "${env.GRADLE_REMOTE_CACHE_USERNAME}"
        GRADLE_REMOTE_CACHE_PASSWORD = "${env.GRADLE_REMOTE_CACHE_PASSWORD}"
// URL кэша из системных переменных Jenkins (если задан)
        GRADLE_REMOTE_CACHE_URL = "${env.GRADLE_REMOTE_CACHE_URL ?: 'http://192.168.0.109:5071/cache/'}"
        DOTENV_FILE = "/var/agent-jdk21/env/.env.develop"
    }

    tools {
        git 'Default'
    }

    stages {
        stage('Init') {
            steps {
                script {
                     runGradleTask = load 'scripts/gradleUtils.groovy'
                     sh 'chmod +x ./gradlew'
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

// refresh-dependencies заставит Gradle перезагрузить зависимости и записать их в удалённый кэш.
        stage('Build') {
            steps {
                script {
                    try {
                        sh """
                            ./gradlew clean build \\
                            --build-cache \\
                            --refresh-dependencies \\
                            --info \\
                            --debug \\
                            -x test \\
                            -P\"dotenv.filename\"=\"${DOTENV_FILE}\" \\
                            -Dgradle.cache.remote.url=${GRADLE_REMOTE_CACHE_URL} \\
                            -Dgradle.cache.remote.username=${GRADLE_REMOTE_CACHE_USERNAME} \\
                            -Dgradle.cache.remote.password=${GRADLE_REMOTE_CACHE_PASSWORD}
                        """
                        telegramSend(message: "✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nView build: ${env.BUILD_URL}")
                    } catch (e) {
                        telegramSend(message: "❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nView build: ${env.BUILD_URL}")
                        error "Build failed"
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

// ➤➤➤ Добавляем блок post для отправки уведомлений в Telegram
    post {
        always {
            script {
                def buildInfo = "📊 Build Info:\n" +
                        "Job: ${env.JOB_NAME}\n" +
                        "Build #: ${currentBuild.number}\n" +
                        "Status: ${currentBuild.currentResult}\n" +
                        "Duration: ${currentBuild.durationString}\n" +
                        "View build: ${env.BUILD_URL}"
                telegramSend(message: buildInfo)
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