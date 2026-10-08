package com.mision.app.core.text

import org.junit.Assert.assertEquals
import org.junit.Test

class PluralsTest {

    @Test
    fun `uses the singular only for exactly one`() {
        assertEquals("1 día", plural(1, "día", "días"))
        assertEquals("0 días", plural(0, "día", "días"))
        assertEquals("7 días", plural(7, "día", "días"))
    }
}
