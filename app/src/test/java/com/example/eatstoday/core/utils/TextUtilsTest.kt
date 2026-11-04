package com.example.eatstoday.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextUtilsTest {
    @Test fun searchIgnoresVietnameseDiacritics() {
        assertEquals("bun bo hue", "Bún bò Huế".normalizedForSearch())
        assertEquals("dau hu", "Đậu hũ".normalizedForSearch())
    }

    @Test fun passwordNeedsLengthLettersAndDigits() {
        assertTrue(isValidPassword("Eats1234"))
        assertFalse(isValidPassword("short1"))
        assertFalse(isValidPassword("onlyletters"))
        assertFalse(isValidPassword("12345678"))
    }

    @Test fun emailValidation() {
        assertTrue(isValidEmail("demo@eatstoday.vn"))
        assertFalse(isValidEmail("demo@eatstoday"))
        assertFalse(isValidEmail(""))
    }

    @Test fun timerNeverGoesNegative() {
        assertEquals("08:32", formatTimer(512))
        assertEquals("00:00", formatTimer(-5))
    }

    @Test fun quantityHidesTrailingZero() {
        assertEquals("2", formatQuantity(2.0))
        assertEquals("1.5", formatQuantity(1.5))
    }
}
