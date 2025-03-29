pipeline {
    agent { label 'agent1' }
// ➤➤➤ Добавляем блок environment для переменных кэша
    environment {
        // Логин/пароль из хранилища секретов Jenkins (рекомендуемый способ)
        GRADLE_REMOTE_CACHE_USERNAME = "${env.GRADLE_REMOTE_CACHE_USERNAME}"
        GRADLE_REMOTE_CACHE_PASSWORD = "${env.GRADLE_REMOTE_CACHE_PASSWORD}"
        // URL кэша из системных переменных Jenkins (если задан)
        GRADLE_REMOTE_CACHE_URL = "${env.GRADLE_REMOTE_CACHE_URL ?: 'http://192.168.0.109:5071/cache/'}"
    }

    tools {
        git 'Default'
    }

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
                        sh './gradlew checkstyleMain checkstyleTest'
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
                        sh './gradlew compileJava'
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
                        sh './gradlew test'
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
                        sh './gradlew jacocoTestReport jacocoTestCoverageVerification'
                    } catch (e) {
                        telegramSend(message: "Code coverage FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                        error "Code coverage failed"
                    }
                }
            }
        }
//--refresh-dependencies заставит Gradle перезагрузить зависимости и записать их в удалённый кэш.
        stage('Build') {
            steps { //шаг в Jenkins pipeline:
                script {
                    try {
                        sh '''
                             ./gradlew clean build \
                             --build-cache \
                             --refresh-dependencies \
                             --info \
                             -x test \
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
    }

    post { //this is post bloc for telegram
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
