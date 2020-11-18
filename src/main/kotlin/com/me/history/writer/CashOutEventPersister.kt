package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.*
import org.apache.logging.log4j.LogManager
import org.postgresql.util.PSQLException
import java.sql.Connection

class CashOutEventPersister(private val connection: Connection) {
    companion object {
        private val LOGGER = LogManager.getLogger(CashOutEventPersister::class.java.name)
        private const val DUPLICATE_KEY_SQL_CODE = "23505"
    }

    private val messagePersister = MessagePersister(connection)
    private val balancePersister = BalancePersister(connection)
    private val cashOutPersister = CashOutPersister(connection)
    private val feeInstructionsPersister = FeeInstructionsPersister(connection)
    private val feeTransfersPersister = FeeTransfersPersister(connection)

    fun persistCashOutEvent(cashOutEvent: CashOutEvent): Boolean {
        return try {
            val messageId = try {
                messagePersister.persistMessage(cashOutEvent.header)
            } catch (e: PSQLException) {
                if (e.sqlState == DUPLICATE_KEY_SQL_CODE) {
                    LOGGER.info("Message ${cashOutEvent.header.messageId}, sequence ${cashOutEvent.header.sequenceNumber} already persisted")
                    connection.rollback()
                    return true
                }
                throw e
            }
            val balanceUpdateId = balancePersister.persistBalances(messageId, cashOutEvent.header.sequenceNumber, cashOutEvent.header.messageType, cashOutEvent.header.timestamp, cashOutEvent.balanceUpdatesList)
            val cashOutId = cashOutPersister.persistCashOut(messageId, cashOutEvent.header.sequenceNumber, balanceUpdateId, cashOutEvent.cashOut)
            cashOutEvent.cashOut.feesList.forEach {
                feeInstructionsPersister.persistCashOutFeeInstruction(messageId, cashOutEvent.header.sequenceNumber, cashOutEvent.cashOut.brokerId, it.instruction, cashOutId)
                feeTransfersPersister.persistCashOutFeeTransfer(messageId, cashOutEvent.header.sequenceNumber, cashOutEvent.cashOut.brokerId, it.transfer, cashOutId)
            }
            connection.commit()
            true
        } catch (e: Exception) {
            LOGGER.error("Unable to persist: ${e.message}. $cashOutEvent", e)
            connection.rollback()
            false
        }
    }
}