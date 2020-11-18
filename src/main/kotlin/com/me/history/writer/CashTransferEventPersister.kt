package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.CashTransferEvent
import org.apache.logging.log4j.LogManager
import org.postgresql.util.PSQLException
import java.sql.Connection

class CashTransferEventPersister(private val connection: Connection) {
    companion object {
        private val LOGGER = LogManager.getLogger(CashTransferEventPersister::class.java.name)
        private const val DUPLICATE_KEY_SQL_CODE = "23505"
    }

    private val messagePersister = MessagePersister(connection)
    private val balancePersister = BalancePersister(connection)
    private val cashTransferPersister = CashTransferPersister(connection)
    private val feeInstructionsPersister = FeeInstructionsPersister(connection)
    private val feeTransfersPersister = FeeTransfersPersister(connection)

    fun persistCashTransferEvent(cashTransferEvent: CashTransferEvent): Boolean {
        return try {
            val messageId = try {
                messagePersister.persistMessage(cashTransferEvent.header)
            } catch (e: PSQLException) {
                if (e.sqlState == DUPLICATE_KEY_SQL_CODE) {
                    LOGGER.info("Message ${cashTransferEvent.header.messageId}, sequence ${cashTransferEvent.header.sequenceNumber} already persisted")
                    connection.rollback()
                    return true
                }
                throw e
            }
            val balanceUpdateId = balancePersister.persistBalances(messageId, cashTransferEvent.header.sequenceNumber, cashTransferEvent.header.messageType, cashTransferEvent.header.timestamp, cashTransferEvent.balanceUpdatesList)
            val cashTransferId = cashTransferPersister.persistCashTransfer(messageId, cashTransferEvent.header.sequenceNumber, balanceUpdateId, cashTransferEvent.cashTransfer)
            cashTransferEvent.cashTransfer.feesList.forEach {
                feeInstructionsPersister.persistCashTransferFeeInstruction(messageId, cashTransferEvent.header.sequenceNumber, cashTransferEvent.cashTransfer.brokerId, it.instruction, cashTransferId)
                feeTransfersPersister.persistCashTransferFeeTransfer(messageId, cashTransferEvent.header.sequenceNumber, cashTransferEvent.cashTransfer.brokerId, it.transfer, cashTransferId)
            }
            connection.commit()
            true
        } catch (e: Exception) {
            LOGGER.error("Unable to persist: ${e.message}. $cashTransferEvent", e)
            connection.rollback()
            false
        }
    }
}