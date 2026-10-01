package com.mision.app.domain.repository

/**
 * Runs a multi-repository operation atomically.
 *
 * Completing a mission or buying an item touches several tables; without a
 * transaction a double tap could interleave two runs and grant the same
 * reward twice, and a crash halfway would leave the data inconsistent.
 */
interface TransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}
