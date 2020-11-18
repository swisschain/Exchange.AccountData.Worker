package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import org.apache.logging.log4j.LogManager
import org.postgresql.util.PSQLException
import java.sql.Connection

class CashInEventPersister(private val connection: Connection) {

    companion object {
        private val LOGGER = LogManager.getLogger(CashInEventPersister::class.java.name)
        private const val DUPLICATE_KEY_SQL_CODE = "23505"
    }

    private val messagePersister = MessagePersister(connection)
    private val balancePersister = BalancePersister(connection)
    private val cashInPersister = CashInPersister(connection)
    private val feeInstructionsPersister = FeeInstructionsPersister(connection)
    private val feeTransfersPersister = FeeTransfersPersister(connection)

    fun persistCashInEvent(cashInEvent: OutgoingMessages.CashInEvent): Boolean {
        return try {
            val messageId = try {
                messagePersister.persistMessage(cashInEvent.header)
            } catch (e: PSQLException) {
                if (e.sqlState == DUPLICATE_KEY_SQL_CODE) {
                    LOGGER.info("Message ${cashInEvent.header.messageId}, sequence ${cashInEvent.header.sequenceNumber} already persisted")
                    connection.rollback()
                    return true
                }
                throw e
            }
            val balanceUpdateId = balancePersister.persistBalances(messageId, cashInEvent.header.sequenceNumber, cashInEvent.header.messageType, cashInEvent.header.timestamp, cashInEvent.balanceUpdatesList)
            val cashInId = cashInPersister.persistCashIn(messageId, cashInEvent.header.sequenceNumber, balanceUpdateId, cashInEvent.cashIn)
            cashInEvent.cashIn.feesList.forEach {
                feeInstructionsPersister.persistCashInFeeInstruction(messageId, cashInEvent.header.sequenceNumber, cashInEvent.cashIn.brokerId, it.instruction, cashInId)
                feeTransfersPersister.persistCashInFeeTransfer(messageId, cashInEvent.header.sequenceNumber, cashInEvent.cashIn.brokerId, it.transfer, cashInId)
            }
            connection.commit()
            true
        } catch (e: Exception) {
            LOGGER.error("Unable to persist: ${e.message}. $cashInEvent", e)
            connection.rollback()
            false
        }
    }
}