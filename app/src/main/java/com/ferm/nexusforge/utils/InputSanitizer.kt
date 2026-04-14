package com.ferm.nexusforge.utils

/**
 * Утилита для санитизации входных данных и предотвращения атак
 */
object InputSanitizer {
    
    /**
     * Санитизирует имя модпака для предотвращения path traversal атак
     * Удаляет опасные символы типа ../, ..\ и специальные символы пути
     * @param name Исходное имя модпака
     * @return Безопасное имя для использования в путях файлов
     */
    fun sanitizeModpackName(name: String): String {
        if (name.isBlank()) return "modpack"
        
        return name
            // Удаляем попытки path traversal
            .replace(Regex("""\.\.[\\/]"""), "")
            .replace(Regex("""\.\."""), "")
            // Удаляем опасные символы
            .replace(Regex("""[<>:"|?*\\/]"""), "")
            // Удаляем управляющие символы
            .replace(Regex("""[\x00-\x1f\x7f]"""), "")
            // Заменяем пробелы на подчеркивания
            .replace(" ", "_")
            // Оставляем только буквы, цифры, подчеркивание, дефис
            .replace(Regex("""[^a-zA-Z0-9_\-]"""), "")
            // Удаляем ведущие/конечные подчеркивания и дефисы
            .trim('_', '-')
            // Гарантируем что результат не пустой
            .takeIf { it.isNotEmpty() } ?: "modpack"
    }
    
    /**
     * Санитизирует поисковый запрос для предотвращения injection атак
     * @param query Исходный поисковый запрос
     * @return Безопасный запрос для API вызовов
     */
    fun sanitizeSearchQuery(query: String): String {
        if (query.isBlank()) return ""
        
        return query
            // Ограничиваем длину для предотвращения abuse
            .take(100)
            // Удаляем управляющие символы
            .replace(Regex("""[\x00-\x1f\x7f]"""), "")
            // Обрезаем пробелы
            .trim()
    }
    
    /**
     * Проверяет валидность длины имени модпака
     * @param name Имя модпака для проверки
     * @return true если валидно, false иначе
     */
    fun isValidModpackName(name: String): Boolean {
        return name.isNotBlank() && name.length in 1..50
    }
    
    /**
     * Проверяет валидность длины поискового запроса
     * @param query Поисковый запрос для проверки
     * @return true если валидно, false иначе
     */
    fun isValidSearchQuery(query: String): Boolean {
        return query.length in 1..100
    }
}
