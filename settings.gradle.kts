rootProject.name = "DevOps"

//Enable cache for building Gradle on remoted server
buildCache {
    remote<HttpBuildCache> {
        url = uri(System.getenv("GRADLE_REMOTE_CACHE_URL") ?: "")
        isAllowInsecureProtocol = true
        isAllowUntrustedServer = true
        isPush = System.getenv("GRADLE_REMOTE_CACHE_PUSH").toBoolean()
        credentials {
            username = System.getenv("GRADLE_REMOTE_CACHE_USERNAME") ?: ""
            password = System.getenv("GRADLE_REMOTE_CACHE_PASSWORD") ?: ""
        }
    }
}