package com.mision.app.core.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Injectable clock. Business rules never read the wall clock directly so date
 * logic can be tested deterministically (midnight rollovers, missed days, ...).
 */
interface ClockProvider {
    fun today(): LocalDate
    fun todayEpochDay(): Int = today().toEpochDay().toInt()
    fun now(): LocalDateTime
    fun nowEpochSecond(): Long
    fun zone(): ZoneId
}

class SystemClockProvider : ClockProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun nowEpochSecond(): Long = now().atZone(zone()).toEpochSecond()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
