rootProject.name = "DevOps"

// Конфигурация кеша сборки для ускорения процесса
buildCache {
    local {
        isEnabled = false // Отключаем локальный кеш, так как он не нужен в CI
    }
    remote(HttpBuildCache::class) {
      //  url = uri(System.getenv("GRADLE_REMOTE_CACHE_URL") ?: "http://192.168.0.109:5071/")  // Указываем URL для удаленного кэша
       //al cacheUrl = System.getenv("GRADLE_REMOTE_CACHE_URL")

        // Проверяем наличие переменной окружения для удалённого кеша
        val cacheUrl = System.getenv("GRADLE_REMOTE_CACHE_URL")
        if (cacheUrl != null) {
            url = uri(cacheUrl)
            isPush = true // Разрешаем запись в кеш
            // Комментарий: Избегайте небезопасных настроек в продакшене.
            // Включите только если ваш сервер кеша действительно требует HTTP или ненадёжен.
            // isAllowInsecureProtocol = true
            // isAllowUntrustedServer = true
        } else {
            isEnabled = false // Отключаем удалённый кеш, если URL не задан
        }
    }
}

