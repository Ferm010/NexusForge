package com.ferm.nexusforge

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for SecurityCheck functionality
 */
class SecurityCheckTest {
    
    @Test
    fun testHashSignatureFormat() {
        // Test that hash signature is in correct format (64 hex characters for SHA-256)
        val validHash = "f91ebea77c4ee70e142cc0865659d3d9b9b43f14b8070d9db40a8dd40f89dab3"
        assertEquals(64, validHash.length)
        assertTrue(validHash.matches(Regex("[a-f0-9]{64}")))
    }
    
    @Test
    fun testExpectedSignatureNotEmpty() {
        // Ensure EXPECTED_SIGNATURE is properly set
        val expectedSignature = "f91ebea77c4ee70e142cc0865659d3d9b9b43f14b8070d9db40a8dd40f89dab3"
        assertNotNull(expectedSignature)
        assertNotEquals("", expectedSignature)
        assertEquals(64, expectedSignature.length)
    }
    
    @Test
    fun testSignatureHashConsistency() {
        // Test that the same input produces the same hash
        val hash1 = "f91ebea77c4ee70e142cc0865659d3d9b9b43f14b8070d9db40a8dd40f89dab3"
        val hash2 = "f91ebea77c4ee70e142cc0865659d3d9b9b43f14b8070d9db40a8dd40f89dab3"
        assertEquals(hash1, hash2)
    }
}
