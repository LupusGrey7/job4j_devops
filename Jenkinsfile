pipeline {
    agent { label 'agent1' }

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
                        sh './gradlew clean build --build-cache --refresh-dependencies -x test' // Пропускаем тесты, так как они уже выполнены
                        telegramSend(message: "✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n" +
                                            "View build: ${env.BUILD_URL}")
                    } catch (e) {
                        telegramSend(message: "❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n" +
                                            "View build: ${env.BUILD_URL}")
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
