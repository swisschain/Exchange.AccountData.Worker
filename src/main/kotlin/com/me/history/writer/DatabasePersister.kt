package com.me.history.writer

import com.me.history.writer.config.DatabaseConfig
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.CashInEvent
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.CashOutEvent
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.CashTransferEvent
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.ExecutionEvent
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.MessageType
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.MessageType.CASH_IN
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.MessageType.CASH_OUT
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.MessageType.CASH_TRANSFER
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.MessageType.ORDER
import org.apache.logging.log4j.LogManager
import java.sql.Connection
import java.sql.DriverManager

class DatabasePersister(jdbcConfig: DatabaseConfig) {
    companion object {
        private val LOGGER = LogManager.getLogger(DatabasePersister::class.java.name)
    }

    private val connection = initConnection(jdbcConfig)
    private val executionEventPersister = ExecutionEventPersister(connection)
    private val cashInEventPersister = CashInEventPersister(connection)
    private val cashOutEventPersister = CashOutEventPersister(connection)
    private val cashTransferEventPersister = CashTransferEventPersister(connection)

    private fun initConnection(config: DatabaseConfig): Connection {
        val connection = DriverManager.getConnection(config.url, config.user, config.password)
        connection.autoCommit = false
        return connection
    }

    fun persistMessage(messageType: MessageType, message: ByteArray): Boolean {
        when (messageType) {
            CASH_IN -> return persistCashIn(message)
            CASH_OUT -> return persistCashOut(message)
            CASH_TRANSFER -> return persistCashTransfer(message)
            ORDER -> return persistExecutionEvent(message)
            else -> LOGGER.info("Unknown message type: ${messageType.name}")
        }
        return false
    }

    private fun persistCashIn(rawCashIn: ByteArray): Boolean {
        val cashIn = CashInEvent.parseFrom(rawCashIn)
        return cashInEventPersister.persistCashInEvent(cashIn)
    }

    private fun persistCashOut(rawCashOut: ByteArray): Boolean {
        val cashOut = CashOutEvent.parseFrom(rawCashOut)
        return cashOutEventPersister.persistCashOutEvent(cashOut)
    }

    private fun persistCashTransfer(rawCashTransfer: ByteArray): Boolean {
        val cashTransfer = CashTransferEvent.parseFrom(rawCashTransfer)
        return cashTransferEventPersister.persistCashTransferEvent(cashTransfer)
    }

    private fun persistExecutionEvent(rawExecutionEvent: ByteArray): Boolean {
        val executionEvent = ExecutionEvent.parseFrom(rawExecutionEvent)
        return executionEventPersister.persistExecutionEvent(executionEvent)
    }
}