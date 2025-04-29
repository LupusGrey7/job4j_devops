pipeline {
    agent { label 'agent1' }

    environment {
        GRADLE_REMOTE_CACHE_USERNAME = "${env.GRADLE_REMOTE_CACHE_USERNAME}"
        GRADLE_REMOTE_CACHE_PASSWORD = "${env.GRADLE_REMOTE_CACHE_PASSWORD}"
        GRADLE_REMOTE_CACHE_URL = "${env.GRADLE_REMOTE_CACHE_URL ?: 'http://192.168.0.109:5071/cache/'}"
        DOTENV_FILE = "/var/agent-jdk21/env/.env.develop"
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
                    runGradleTask('checkstyleMain checkstyleTest', 'Checkstyle FAILED')
                }
            }
        }

        stage('Compile') {
            steps {
                script {
                    runGradleTask('compileJava', 'Compilation FAILED')
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    runGradleTask('test', 'Tests FAILED')
                }
            }
        }

        stage('Code Coverage') {
            steps {
                script {
                    runGradleTask('jacocoTestReport jacocoTestCoverageVerification', 'Code coverage FAILED')
                }
            }
        }

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
                            -Dgradle.cache.remote.url=$GRADLE_REMOTE_CACHE_URL \\
                            -Dgradle.cache.remote.username=$GRADLE_REMOTE_CACHE_USERNAME \\
                            -Dgradle.cache.remote.password=$GRADLE_REMOTE_CACHE_PASSWORD
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
                    runGradleTask('update', 'Update DB FAILED')
                }
            }
        }
    }

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

def runGradleTask(String gradleTasks, String failMessage) {
    try {
        sh "./gradlew ${gradleTasks} -P\"dotenv.filename\"=\"${DOTENV_FILE}\""
    } catch (e) {
        echo "Error occurred while running Gradle task '${gradleTasks}': ${e.getMessage()}"
        telegramSend(message: "${failMessage}: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nError: ${e.getMessage()}")
        error "${failMessage}: ${e.getMessage()}"
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