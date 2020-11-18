package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import org.apache.logging.log4j.LogManager
import org.postgresql.util.PSQLException
import java.sql.Connection

class ExecutionEventPersister(private val connection: Connection) {
    companion object {
        private val LOGGER = LogManager.getLogger(ExecutionEventPersister::class.java.name)
        private const val DUPLICATE_KEY_SQL_CODE = "23505"
    }

    private val messagePersister = MessagePersister(connection)
    private val balancePersister = BalancePersister(connection)
    private val orderPersister = OrderPersister(connection)

    fun persistExecutionEvent(executionEvent: OutgoingMessages.ExecutionEvent): Boolean {
        return try {
            val messageId = try {
                messagePersister.persistMessage(executionEvent.header)
            } catch (e: PSQLException) {
                if (e.sqlState == DUPLICATE_KEY_SQL_CODE) {
                    LOGGER.info("Message ${executionEvent.header.messageId}, sequence ${executionEvent.header.sequenceNumber} already persisted")
                    connection.rollback()
                    return true
                }
                throw e
            }
            balancePersister.persistBalances(messageId, executionEvent.header.sequenceNumber, executionEvent.header.messageType, executionEvent.header.timestamp, executionEvent.balanceUpdatesList)
            orderPersister.persistOrders(messageId, executionEvent.header.sequenceNumber, executionEvent.ordersList)
            connection.commit()
            true
        } catch (e: Exception) {
            LOGGER.error("Unable to persist: ${e.message}. $executionEvent", e)
            connection.rollback()
            false
        }
    }
}