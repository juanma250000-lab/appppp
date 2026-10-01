package com.mision.app.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderCopyTest {

    @Test
    fun `an evening reminder never claims the day is done while missions are pending`() {
        val (title, body) = MisionNotifier.reminderCopy(pendingMissions = 2, hour = 20)

        assertEquals("Completa tus misiones para mantener tu racha", title)
        assertEquals("Te quedan 2 misiones para cerrar el día.", body)
    }

    @Test
    fun `a finished day is celebrated instead of nagging`() {
        assertEquals("¡Día completado!", MisionNotifier.reminderCopy(pendingMissions = 0, hour = 20).first)
    }

    @Test
    fun `one pending mission uses the singular`() {
        assertEquals("Te queda 1 misión para cerrar el día.", MisionNotifier.reminderCopy(1, 15).second)
    }

    @Test
    fun `an unknown count falls back to a neutral reminder`() {
        assertEquals("Tus misiones te esperan", MisionNotifier.reminderCopy(null, 20).first)
    }
}
