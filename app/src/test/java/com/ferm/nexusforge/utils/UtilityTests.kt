package com.ferm.nexusforge.utils

import com.ferm.nexusforge.viewmodels.EmailValidator
import com.ferm.nexusforge.utils.InputSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilityTests {

    @Test
    fun `givenValidEmailFormat_shouldReturnTrue`() {
        val validator = EmailValidator()
        assertTrue("Должен принимать стандартные email", 
                   validator.isValidFormat("user@example.com"))
        assertTrue("Должен принимать email с цифрами и точками", 
                   validator.isValidFormat("test.user123@domain.org"))
    }

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

    @Test
    fun `sanitizeModpackName_removesPathTraversalAndDangerousChars`() {
        val maliciousInput = "My Modpack/../../../system32"
        val safeOutput = InputSanitizer.sanitizeModpackName(maliciousInput)

        assertFalse("Путь ../ должен быть удален", 
                    safeOutput.contains("../"))

        assertEquals("Результат санитизации не совпадает", 
                     "My_Modpack_system32", safeOutput)
    }

    @Test
    fun `sanitizeSearchQuery_enforcesLengthAndRemovesControlChars`() {
        val longQuery = "a".repeat(200)
        assertEquals("Длина запроса не должна превышать 100 символов", 
                     100, InputSanitizer.sanitizeSearchQuery(longQuery).length)

        val queryWithControlChars = "Minecraft\u0001Mod"
        val sanitized = InputSanitizer.sanitizeSearchQuery(queryWithControlChars)
        
        assertFalse("Управляющие символы должны быть удалены", 
                    sanitized.contains('\u0001'))
    }
}
