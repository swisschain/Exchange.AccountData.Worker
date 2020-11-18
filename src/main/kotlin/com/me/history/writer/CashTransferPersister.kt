package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import removeExtraWhitespaces
import java.sql.Connection

class CashTransferPersister (connection: Connection) {

    private val INSERT_MESSAGE = """
        INSERT INTO cash_transfers (message_id, sequence_number, balance_update_id, broker_id, account_id, from_wallet_id, to_wallet_id, asset_id, volume, overdraftLimit, description)
        VALUES                     (?         , ?              , ?                , ?        , ?         , ?             , ?           , ?       , ?     , ?             , ?)
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatement = connection.prepareStatement(INSERT_MESSAGE)

    fun persistCashTransfer(messageId: Long, sequenceId: Long, balanceUpdateId: Long, cashTransfer: OutgoingMessages.CashTransferEvent.CashTransfer): Long {
        var i = 1
        preparedStatement.setLong(i++, messageId)
        preparedStatement.setLong(i++, sequenceId)
        preparedStatement.setLong(i++, balanceUpdateId)
        preparedStatement.setString(i++, cashTransfer.brokerId)
        preparedStatement.setLong(i++, cashTransfer.accountId)
        preparedStatement.setLong(i++, cashTransfer.fromWalletId)
        preparedStatement.setLong(i++, cashTransfer.toWalletId)
        preparedStatement.setString(i++, cashTransfer.assetId)
        preparedStatement.setString(i++, cashTransfer.volume)
        preparedStatement.setString(i++, cashTransfer.overdraftLimit)
        preparedStatement.setString(i, cashTransfer.description)

        val rs = preparedStatement.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get cash out id")
    }
}