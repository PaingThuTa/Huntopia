package com.example.huntopia

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementRepositoryTest {

    @Test
    fun isValidCode_acceptsFourDigits() {
        assertTrue(AchievementRepository.isValidCode("1001"))
        assertTrue(AchievementRepository.isValidCode(" 1024 "))
    }

    @Test
    fun isValidCode_rejectsInvalidQrValues() {
        assertFalse(AchievementRepository.isValidCode("999"))
        assertFalse(AchievementRepository.isValidCode("10025"))
        assertFalse(AchievementRepository.isValidCode("ABCD"))
        assertFalse(AchievementRepository.isValidCode("10A2"))
    }
}
