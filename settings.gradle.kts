rootProject.name = "DevOps"

//Enable cache for building Gradle on remoted server
buildCache {
    local {
        isEnabled = false  // Работает в Gradle 6.6+, отключаем локальное кэширование, если не требует
    }
    remote(HttpBuildCache::class) {
        url = uri(System.getenv("GRADLE_REMOTE_CACHE_URL") ?: "http://192.168.0.109:5071/")  // Указываем URL для удаленного кэша
        isAllowInsecureProtocol = true // Разрешить небезопасные HTTP-соединения
        isAllowUntrustedServer = true // Разрешить ненадежные серверы
        isPush = true // Разрешить запись в кэш
    }
}

