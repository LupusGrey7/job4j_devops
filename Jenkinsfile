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
// ➤➤➤ Добавляем блок stages
    stages {
        stage('Prepare Environment') {
            steps {
                script {
                    sh 'chmod +x ./gradlew'
                }
            }
        }

        stage('Checkstyle') {
            steps {
                script {
                    try {
                        sh "./gradlew checkstyleMain checkstyleTest -P\"dotenv.filename\"=\"${DOTENV_FILE}\""
                    } catch (e) {
                        telegramSend(message: "Checkstyle FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                        error "Checkstyle failed"
                    }
                }
            }
        }

        stage('Compile') {
            steps {
                script {
                    try {
                         sh "./gradlew compileJava -P\"dotenv.filename\"=\"${DOTENV_FILE}\""
                    } catch (e) {
                        telegramSend(message: "Compilation FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                        error "Compilation failed"
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    try {
                        sh "./gradlew test -P\"dotenv.filename\"=\"${DOTENV_FILE}\""
                    } catch (e) {
                        telegramSend(message: "Tests FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                        error "Tests failed"
                    }
                }
            }
        }

        stage('Code Coverage') {
            steps {
                script {
                    try {
                        sh "./gradlew jacocoTestReport jacocoTestCoverageVerification -P\"dotenv.filename\"=\"${DOTENV_FILE}\""
                    } catch (e) {
                        telegramSend(message: "Code coverage FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                        error "Code coverage failed"
                    }
                }
            }
        }
// refresh-dependencies заставит Gradle перезагрузить зависимости и записать их в удалённый кэш.
        stage('Build') {
            steps { //шаг в Jenkins pipeline: // ➤ Добавлено опциональное условие --debug \
                script {
                    try {
                        sh '''
                             ./gradlew clean build \
                             --build-cache \
                             --refresh-dependencies \
                             --info \
                             --debug \
                             -x test \
                             -P\"dotenv.filename\"=\"${DOTENV_FILE}\" \
                             -Dgradle.cache.remote.url=$GRADLE_REMOTE_CACHE_URL \
                             -Dgradle.cache.remote.username=$GRADLE_REMOTE_CACHE_USERNAME \
                             -Dgradle.cache.remote.password=$GRADLE_REMOTE_CACHE_PASSWORD
                        '''
                        telegramSend(message: "✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nView build: ${env.BUILD_URL}")
                    } catch (e) {
                        telegramSend(message: "❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nView build: ${env.BUILD_URL}")
                        error "Build failed"
                    }
                }
            }
        }
// ➤➤➤ добавим новый этап, который будет загружать данные в базу:
        stage('Update DB') {
            steps {
                script {
                    sh './gradlew update -P"dotenv.filename"="/var/agent-jdk21/env/.env.develop"'
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
            // Можно добавить дополнительные действия при успехе
        }

        failure {
            echo "Build failed!"
            // Можно добавить дополнительные действия при неудаче
        }

        unstable {
            echo "Build unstable!"
            telegramSend(message: "⚠️ Build UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
    }
}
