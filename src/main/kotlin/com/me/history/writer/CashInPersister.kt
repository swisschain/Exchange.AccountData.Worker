package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.CashInEvent.CashIn
import removeExtraWhitespaces
import java.sql.Connection

class CashInPersister (connection: Connection) {

    private val INSERT_MESSAGE = """
        INSERT INTO cash_ins (message_id, sequence_number, balance_update_id, broker_id, account_id, wallet_id, asset_id, volume, description)
        VALUES               (?         , ?              , ?                , ?        , ?         , ?        , ?       , ?     , ?)
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatement = connection.prepareStatement(INSERT_MESSAGE)

    fun persistCashIn(messageId: Long, sequenceId: Long, balanceUpdateId: Long, cashIn: CashIn): Long {
        var i = 1
        preparedStatement.setLong(i++, messageId)
        preparedStatement.setLong(i++, sequenceId)
        preparedStatement.setLong(i++, balanceUpdateId)
        preparedStatement.setString(i++, cashIn.brokerId)
        preparedStatement.setLong(i++, cashIn.accountId)
        preparedStatement.setLong(i++, cashIn.walletId)
        preparedStatement.setString(i++, cashIn.assetId)
        preparedStatement.setString(i++, cashIn.volume)
        preparedStatement.setString(i, cashIn.description)

        val rs = preparedStatement.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get cash in id")
    }
}