package com.ferm.nexusforge.utils

import com.ferm.nexusforge.viewmodels.EmailValidator
import com.ferm.nexusforge.utils.InputSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Модульные тесты для проверки логики валидации и санитизации данных.
 * Используют JUnit 4 (как указано в build.gradle.kts).
 */
class UtilityTests {

    // ==========================================
    // ТЕСТ 1: Валидация Email (Корректный ввод)
    // ==========================================
    @Test
    fun `givenValidEmailFormat_shouldReturnTrue`() {
        val validator = EmailValidator()
        assertTrue("Должен принимать стандартные email", 
                   validator.isValidFormat("user@example.com"))
        assertTrue("Должен принимать email с цифрами и точками", 
                   validator.isValidFormat("test.user123@domain.org"))
    }

    // ==========================================
    // ТЕСТ 2: Валидация Email (Некорректный ввод)
    // ==========================================
    @Test
    fun `givenInvalidEmailFormat_shouldReturnFalse`() {
        val validator = EmailValidator()
        assertFalse("Пустой email должен быть невалидным", 
                    validator.isValidFormat(""))
        assertFalse("Отсутствие символа @ должно делать email невалидным", 
                    validator.isValidFormat("not-an-email"))
        assertFalse("Отсутствие домена после @ должно делать email невалидным", 
                    validator.isValidFormat("@missing-local.com"))
        assertFalse("Пробелы в email должны быть запрещены", 
                    validator.isValidFormat("spaces in@email.com"))
    }

    // ==========================================
    // ТЕСТ 3: Санитизация имени модпака (Path Traversal)
    // ==========================================
    @Test
    fun `sanitizeModpackName_removesPathTraversalAndDangerousChars`() {
        // Имитация попытки атаки через path traversal и опасные символы
        val maliciousInput = "My Modpack/../../../system32"
        val safeOutput = InputSanitizer.sanitizeModpackName(maliciousInput)

        // Проверка, что попытка выйти за пределы папки удалена
        assertFalse("Путь ../ должен быть удален", 
                    safeOutput.contains("../"))
        
        // Проверка корректного результата (пробелы -> _, спецсимволы удалены)
        assertEquals("Результат санитизации не совпадает", 
                     "My_Modpack_system32", safeOutput)
    }

    // ==========================================
    // ТЕСТ 4: Санитизация поискового запроса (Длина и спецсимволы)
    // ==========================================
    @Test
    fun `sanitizeSearchQuery_enforcesLengthAndRemovesControlChars`() {
        // 1. Проверка ограничения длины (макс 100 символов)
        val longQuery = "a".repeat(200)
        assertEquals("Длина запроса не должна превышать 100 символов", 
                     100, InputSanitizer.sanitizeSearchQuery(longQuery).length)

        // 2. Проверка удаления управляющих (непечатаемых) символов
        val queryWithControlChars = "Minecraft\u0001Mod"
        val sanitized = InputSanitizer.sanitizeSearchQuery(queryWithControlChars)
        
        assertFalse("Управляющие символы должны быть удалены", 
                    sanitized.contains('\u0001'))
    }
}
