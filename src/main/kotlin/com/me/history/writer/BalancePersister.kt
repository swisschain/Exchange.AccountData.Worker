package com.me.history.writer

import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.*
import removeExtraWhitespaces
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant

class BalancePersister (connection: Connection) {
    private val INSERT_BALANCE = """
        INSERT INTO balances (message_id, sequence_number, broker_id, account_id, wallet_id, asset_id, balance, reserved, timestamp)
        VALUES               (?         , ?              , ?        , ?         , ?        , ?       , ?      , ?       , ?) 
        ON CONFLICT (broker_id, wallet_id, asset_id) 
        DO UPDATE 
        SET message_id = ?, sequence_number = ?, balance = ?, reserved = ?, timestamp = ?;
    """.removeExtraWhitespaces().trimIndent()

    private val INSERT_BALANCE_UPDATE = """
        INSERT INTO balance_updates (message_id, sequence_number, broker_id, account_id, wallet_id, event_type, asset_id, balance, old_balance, reserved, old_reserved, timestamp)
        VALUES                      (?         , ?              , ?        , ?         , ?        , ?         , ?       , ?      , ?          ,?        , ?           , ?) 
        ON CONFLICT (sequence_number, broker_id, wallet_id, asset_id) 
        DO NOTHING
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatementBalance = connection.prepareStatement(INSERT_BALANCE)
    private val preparedStatementBalanceUpdate = connection.prepareStatement(INSERT_BALANCE_UPDATE)

    fun persistBalances(messageId: Long, sequenceNumber: Long, eventType: Int, timestamp: com.google.protobuf.Timestamp, balances: List<BalanceUpdate>): Long {
        var balanceUpdateId = 0L
        balances.forEach {
            persistBalance(messageId, sequenceNumber, timestamp, it)
            balanceUpdateId = persistBalanceUpdate(messageId, sequenceNumber, eventType, timestamp, it)
        }
        return balanceUpdateId
    }

    private fun persistBalance(messageId: Long, sequenceNumber: Long, timestamp: com.google.protobuf.Timestamp, balance: BalanceUpdate) {
        var i = 1
        preparedStatementBalance.setLong(i++, messageId)
        preparedStatementBalance.setLong(i++, sequenceNumber)
        preparedStatementBalance.setString(i++, balance.brokerId)
        preparedStatementBalance.setLong(i++, balance.accountId)
        preparedStatementBalance.setLong(i++, balance.walletId)
        preparedStatementBalance.setString(i++, balance.assetId)
        preparedStatementBalance.setString(i++, balance.newBalance)
        preparedStatementBalance.setString(i++, balance.newReserved)
        val convertedTimestamp = Timestamp.from(Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong()))
        preparedStatementBalance.setTimestamp(i++, convertedTimestamp)

        preparedStatementBalance.setLong(i++, messageId)
        preparedStatementBalance.setLong(i++, sequenceNumber)
        preparedStatementBalance.setString(i++, balance.newBalance)
        preparedStatementBalance.setString(i++, balance.newReserved)
        preparedStatementBalance.setTimestamp(i, convertedTimestamp)

        preparedStatementBalance.execute()
    }

    private fun persistBalanceUpdate(messageId: Long, sequenceNumber: Long, eventType: Int, timestamp: com.google.protobuf.Timestamp, balance: BalanceUpdate): Long {
        var i = 1
        preparedStatementBalanceUpdate.setLong(i++, messageId)
        preparedStatementBalanceUpdate.setLong(i++, sequenceNumber)
        preparedStatementBalanceUpdate.setString(i++, balance.brokerId)
        preparedStatementBalanceUpdate.setLong(i++, balance.accountId)
        preparedStatementBalanceUpdate.setLong(i++, balance.walletId)
        preparedStatementBalanceUpdate.setInt(i++, eventType)
        preparedStatementBalanceUpdate.setString(i++, balance.assetId)
        preparedStatementBalanceUpdate.setString(i++, balance.newBalance)
        preparedStatementBalanceUpdate.setString(i++, balance.oldBalance)
        preparedStatementBalanceUpdate.setString(i++, balance.newReserved)
        preparedStatementBalanceUpdate.setString(i++, balance.oldReserved)
        preparedStatementBalanceUpdate.setTimestamp(i, Timestamp.from(Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong())))


        val rs = preparedStatementBalanceUpdate.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get message id")
    }
}