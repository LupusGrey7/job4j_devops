// Это вспомогательный groovy-скрипт с функцией для Jenkinsfile
def call(String gradleTasks, String failMessage, String dotenvFile) {
    try {
        sh "./gradlew ${gradleTasks} -P\"dotenv.filename\"=\"${dotenvFile}\""
    } catch (e) {
        echo "Error occurred while running Gradle task '${gradleTasks}': ${e.getMessage()}"
        telegramSend(message: "${failMessage}: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nError: ${e.getMessage()}")
        error "${failMessage}: ${e.getMessage()}"
    }
}