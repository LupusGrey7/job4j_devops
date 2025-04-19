rootProject.name = "DevOps"

//Enable cache for building Gradle on remoted server
buildCache {
    local {
        isEnabled = false  // Работает в Gradle 6.6+, отключаем локальное кэширование, если не требует
    }
    remote<HttpBuildCache> {
        url = uri(System.getenv("GRADLE_REMOTE_CACHE_URL") ?: "http://192.168.0.109:5071/")
        isAllowInsecureProtocol = true // Разрешить небезопасные HTTP-соединения (если не используется HTTPS)
        isAllowUntrustedServer = true
        isPush = true  //System.getenv("GRADLE_REMOTE_CACHE_PUSH").toBoolean() //# Разрешить запись в кэш
        // Настройте Jenkins для анонимного доступа - мы убрали блок credentials
//        credentials {
//            username = System.getenv("GRADLE_REMOTE_CACHE_USERNAME") //?: ""
//            password = System.getenv("GRADLE_REMOTE_CACHE_PASSWORD") //?: ""
//        }
    }
}

