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
        stage('Checkstyle Main') {
            steps {
                script {
                    sh './gradlew checkstyleMain'
                }
            }
        }
        stage('Checkstyle Test') {
            steps {
                script {
                    sh './gradlew checkstyleTest'
                }
            }
        }
        stage('Compile') {
            steps {
                script {
                    sh './gradlew compileJava'
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    sh './gradlew test'
                }
            }
        }
        stage('JaCoCo Report') {
            steps {
                script {
                    sh './gradlew jacocoTestReport'
                }
            }
        }
        stage('JaCoCo Verification') {
            steps {
                script {
                    sh './gradlew jacocoTestCoverageVerification'
                }
            }
        }
        stages {
                stage('Build') {
                    steps {
                        script {
                            try {
                                sh './gradlew build --build-cache'
                                telegramSend(message: "Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                            } catch (e) {
                                telegramSend(message: "Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
                                error "Build failed"
                            }
                        }
                    }
                }
        }

    }
    post { //this is post bloc for telegramm
       always {
           script {
               def buildInfo = "Build number: ${currentBuild.number}\n" +
                                "Build status: ${currentBuild.currentResult}\n" +
                                "Started at: ${new Date(currentBuild.startTimeInMillis)}\n" +
                                "Duration so far: ${currentBuild.durationString}"
               telegramSend(
                    message: buildInfo  // Сообщение для отправки
                )
           }
       }
       success { //do this on success!
           echo "Build succeeded!"
       }
       failure { //to failure
           echo "Build failed!"
       }
    }   
}
