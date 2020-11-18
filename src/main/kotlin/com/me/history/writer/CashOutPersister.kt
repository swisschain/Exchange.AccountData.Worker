package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.CashOutEvent.CashOut
import removeExtraWhitespaces
import java.sql.Connection

class CashOutPersister (connection: Connection) {

    private val INSERT_MESSAGE = """
        INSERT INTO cash_outs (message_id, sequence_number, balance_update_id, broker_id, account_id, wallet_id, asset_id, volume, description)
        VALUES                (?         , ?              , ?                , ?        , ?         , ?        , ?       , ?     , ?)
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatement = connection.prepareStatement(INSERT_MESSAGE)

    fun persistCashOut(messageId: Long, sequenceId: Long, balanceUpdateId: Long, cashOut: CashOut): Long {
        var i = 1
        preparedStatement.setLong(i++, messageId)
        preparedStatement.setLong(i++, sequenceId)
        preparedStatement.setLong(i++, balanceUpdateId)
        preparedStatement.setString(i++, cashOut.brokerId)
        preparedStatement.setLong(i++, cashOut.accountId)
        preparedStatement.setLong(i++, cashOut.walletId)
        preparedStatement.setString(i++, cashOut.assetId)
        preparedStatement.setString(i++, cashOut.volume)
        preparedStatement.setString(i, cashOut.description)

        val rs = preparedStatement.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get cash out id")
    }
}