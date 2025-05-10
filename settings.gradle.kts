rootProject.name = "DevOps" // Указываем имя корневого проекта

// ➤ Конфигурация кеша сборки Gradle
buildCache {
    // 🛑 В CI используем только удалённый кэш, отключаем локальный
    local {
        isEnabled = false
    }

    // ✅ Удалённый HTTP кеш
    remote(HttpBuildCache::class) {
        val cacheUrl = System.getenv("GRADLE_REMOTE_CACHE_URL")
            ?: "http://192.168.0.109:5071/" // 🧪 Тестовый URL, замени на боевой при необходимости

        url = uri(cacheUrl)
        isPush = true // ✅ Разрешаем пушить артефакты в кэш

        // ❗️Ключевой параметр, без него Gradle будет падать
        isAllowInsecureProtocol = cacheUrl.startsWith("http://")

        // 💬 В продакшене обязательно использовать HTTPS и отключить этот флаг
        // isAllowInsecureProtocol = false
    }
}


//rootProject.name = "DevOps"
//
//// Конфигурация кеша сборки для ускорения процесса
//buildCache {
//    local {
//        isEnabled = false // Отключаем локальный кеш, так как он не нужен в CI
//    }
//    remote(HttpBuildCache::class) {
//      //  url = uri(System.getenv("GRADLE_REMOTE_CACHE_URL") ?: "http://192.168.0.109:5071/")  // Указываем URL для удаленного кэша
//       //al cacheUrl = System.getenv("GRADLE_REMOTE_CACHE_URL")
//
//        // Проверяем наличие переменной окружения для удалённого кеша
//        val cacheUrl = uri(System.getenv("GRADLE_REMOTE_CACHE_URL") ?: "http://192.168.0.109:5071/")
//        if (cacheUrl != null) {
//            url = uri(cacheUrl)
//            isPush = true // Разрешаем запись в кеш
//            isAllowInsecureProtocol = true // ✅ Вот это обязательно в коде\ отключаем в Дженкинс требование безопасности использовать только HTTPS
//            // Комментарий: Избегайте небезопасных настроек в продакшене.
//            // Включите только если ваш сервер кеша действительно требует HTTP или ненадёжен.
//            // isAllowInsecureProtocol = true
//            // isAllowUntrustedServer = true
//        } else {
//            isEnabled = false // Отключаем удалённый кеш, если URL не задан
//        }
//    }
//}

