package com.mision.app.data.local

import androidx.room.withTransaction
import com.mision.app.domain.repository.TransactionRunner

/** [TransactionRunner] backed by a Room transaction (writers are serialised). */
class RoomTransactionRunner(private val database: MisionDatabase) : TransactionRunner {
    override suspend fun <R> invoke(block: suspend () -> R): R = database.withTransaction(block)
}
