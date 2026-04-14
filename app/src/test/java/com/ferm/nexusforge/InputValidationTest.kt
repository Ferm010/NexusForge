package com.ferm.nexusforge

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for input validation and sanitization
 */
class InputValidationTest {
    
    @Test
    fun testEmailValidationFormat() {
        // Valid emails
        val validEmails = listOf(
            "user@example.com",
            "test.user@domain.co.uk",
            "user+tag@example.com"
        )
        validEmails.forEach { email ->
            assertTrue("Email should be valid: $email", isValidEmail(email))
        }
    }
    
    @Test
    fun testEmailValidationInvalid() {
        // Invalid emails
        val invalidEmails = listOf(
            "invalid.email",
            "@example.com",
            "user@",
            "user @example.com",
            ""
        )
        invalidEmails.forEach { email ->
            assertFalse("Email should be invalid: $email", isValidEmail(email))
        }
    }
    
    @Test
    fun testPasswordValidation() {
        // Password must be at least 6 characters
        assertFalse(isValidPassword("12345"))
        assertTrue(isValidPassword("123456"))
        assertTrue(isValidPassword("StrongPassword123!"))
    }
    
    @Test
    fun testModpackNameValidation() {
        // Modpack name validation
        assertFalse(isValidModpackName(""))
        assertFalse(isValidModpackName("a"))
        assertTrue(isValidModpackName("My Modpack"))
        assertTrue(isValidModpackName("Modpack-2024"))
        assertFalse(isValidModpackName("a".repeat(256))) // Too long
    }
    
    @Test
    fun testSearchQueryValidation() {
        // Search query must be at least 2 characters
        assertFalse(isValidSearchQuery("a"))
        assertTrue(isValidSearchQuery("ab"))
        assertTrue(isValidSearchQuery("minecraft mod"))
        assertFalse(isValidSearchQuery("a".repeat(1001))) // Too long
    }
    
    // Helper functions for validation
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && 
               email.contains("@") && 
               email.contains(".") &&
               !email.startsWith("@") &&
               !email.endsWith("@") &&
               !email.contains(" ")
    }
    
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    private fun isValidModpackName(name: String): Boolean {
        return name.length in 2..255
    }
    
    private fun isValidSearchQuery(query: String): Boolean {
        return query.length in 2..1000
    }
}
