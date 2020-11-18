package com.me.history.writer

import com.me.history.writer.utils.setNullableString
import com.me.history.writer.utils.setNullableTimestamp
import com.me.history.writer.utils.toSqlTimestamp
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.*
import removeExtraWhitespaces
import java.sql.Connection
import java.sql.Types

class OrderPersister (connection: Connection) {
    private val INSERT_ORDER = """
        INSERT INTO orders (message_id, sequence_number, order_type, me_id, external_id, asset_pair_id, broker_id, account_id, wallet_id, side, volume, remaining_volume, price, status, reject_reason, status_date, created_at, registered_at, last_match_time, lower_limit_price, lower_price, upper_limit_price, upper_price, time_in_force, expiry_time, parent_external_id, child_external_id)
        VALUES             (?         , ?              , ?         , ?    , ?          , ?            , ?        , ?         , ?        , ?   , ?     , ?               , ?    , ?     , ?            , ?          , ?         , ?            , ?              , ?                , ?          , ?                , ?          , ?            , ?          , ?                 , ?) 
        ON CONFLICT (me_id) 
        DO UPDATE 
        SET message_id = ?, sequence_number = ?, remaining_volume = ?, price = ?, status = ?, reject_reason = ?, status_date = ?, last_match_time = ?, parent_external_id = ?, child_external_id = ?
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val INSERT_ORDER_HISTORY = """
        INSERT INTO orders_history (message_id, sequence_number, order_type, me_id, external_id, asset_pair_id, broker_id, account_id, wallet_id, side, volume, remaining_volume, price, status, reject_reason, status_date, created_at, registered_at, last_match_time, lower_limit_price, lower_price, upper_limit_price, upper_price, time_in_force, expiry_time, parent_external_id, child_external_id)
        VALUES                     (?         , ?              , ?         , ?    , ?          , ?            , ?        , ?         , ?        , ?   , ?     , ?               , ?    , ?     , ?            , ?          , ?         , ?            , ?              , ?                , ?          , ?                , ?          , ?            , ?          , ?                 , ?)
        ON CONFLICT (me_id, sequence_number) 
        DO NOTHING
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatementOrder = connection.prepareStatement(INSERT_ORDER)
    private val preparedStatementOrderHistory = connection.prepareStatement(INSERT_ORDER_HISTORY)

    private val tradePersister = TradePersister(connection)
    private val feeInstructionsPersister = FeeInstructionsPersister(connection)

    fun persistOrders(messageId: Long, sequenceNumber: Long, orders: List<ExecutionEvent.Order>) {
        orders.forEach {
            val id = persistOrder(messageId, sequenceNumber, it)
            val historyId = persistOrderHistory(messageId, sequenceNumber, it)
            it.feesList.forEach { feeInstruction ->
                feeInstructionsPersister.persistOrderFeeInstruction(messageId, sequenceNumber, it.brokerId, feeInstruction, id)
            }
            tradePersister.persistTrades(messageId, sequenceNumber, id, it.externalId, historyId, it.brokerId, it.accountId, it.walletId, it.tradesList)
        }
    }

    private fun persistOrder(messageId: Long, sequenceNumber: Long, order: ExecutionEvent.Order): Long {
        var i = 1
        preparedStatementOrder.setLong(i++, messageId)
        preparedStatementOrder.setLong(i++, sequenceNumber)
        preparedStatementOrder.setInt(i++, order.orderType)
        preparedStatementOrder.setString(i++, order.id)
        preparedStatementOrder.setString(i++, order.externalId)
        preparedStatementOrder.setString(i++, order.assetPairId)
        preparedStatementOrder.setString(i++, order.brokerId)
        preparedStatementOrder.setLong(i++, order.accountId)
        preparedStatementOrder.setLong(i++, order.walletId)
        preparedStatementOrder.setInt(i++, order.side)
        preparedStatementOrder.setString(i++, order.volume)
        preparedStatementOrder.setNullableString(i++, order.remainingVolume)
        preparedStatementOrder.setNullableString(i++, order.price)
        preparedStatementOrder.setInt(i++, order.status)
        preparedStatementOrder.setNullableString(i++, order.rejectReason)
        preparedStatementOrder.setTimestamp(i++, order.statusDate.toSqlTimestamp())
        preparedStatementOrder.setTimestamp(i++, order.createdAt.toSqlTimestamp())
        preparedStatementOrder.setTimestamp(i++, order.registered.toSqlTimestamp())
        if (order.hasLastMatchTime()) preparedStatementOrder.setNullableTimestamp(i++, order.lastMatchTime) else preparedStatementOrder.setNull(i++, Types.TIMESTAMP)
        preparedStatementOrder.setNullableString(i++, order.lowerLimitPrice)
        preparedStatementOrder.setNullableString(i++, order.lowerPrice)
        preparedStatementOrder.setNullableString(i++, order.upperLimitPrice)
        preparedStatementOrder.setNullableString(i++, order.upperPrice)
        preparedStatementOrder.setInt(i++, order.timeInForce)
        if (order.hasExpiryTime()) preparedStatementOrder.setNullableTimestamp(i++, order.expiryTime) else preparedStatementOrder.setNull(i++, Types.TIMESTAMP)
        preparedStatementOrder.setNullableString(i++, order.parentExternalId)
        preparedStatementOrder.setNullableString(i++, order.childExternalId)

        preparedStatementOrder.setLong(i++, messageId)
        preparedStatementOrder.setLong(i++, sequenceNumber)
        preparedStatementOrder.setNullableString(i++, order.remainingVolume)
        preparedStatementOrder.setNullableString(i++, order.price)
        preparedStatementOrder.setInt(i++, order.status)
        preparedStatementOrder.setNullableString(i++, order.rejectReason)
        preparedStatementOrder.setTimestamp(i++, order.statusDate.toSqlTimestamp())
        if (order.hasLastMatchTime()) preparedStatementOrder.setNullableTimestamp(i++, order.lastMatchTime) else preparedStatementOrder.setNull(i++, Types.TIMESTAMP)
        preparedStatementOrder.setNullableString(i++, order.parentExternalId)
        preparedStatementOrder.setNullableString(i, order.childExternalId)

        val rs = preparedStatementOrder.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get order id")
    }

    private fun persistOrderHistory(messageId: Long, sequenceNumber: Long, order: ExecutionEvent.Order): Long {
        var i = 1
        preparedStatementOrderHistory.setLong(i++, messageId)
        preparedStatementOrderHistory.setLong(i++, sequenceNumber)
        preparedStatementOrderHistory.setInt(i++, order.orderType)
        preparedStatementOrderHistory.setString(i++, order.id)
        preparedStatementOrderHistory.setString(i++, order.externalId)
        preparedStatementOrderHistory.setString(i++, order.assetPairId)
        preparedStatementOrderHistory.setString(i++, order.brokerId)
        preparedStatementOrderHistory.setLong(i++, order.accountId)
        preparedStatementOrderHistory.setLong(i++, order.walletId)
        preparedStatementOrderHistory.setInt(i++, order.side)
        preparedStatementOrderHistory.setString(i++, order.volume)
        preparedStatementOrderHistory.setNullableString(i++, order.remainingVolume)
        preparedStatementOrderHistory.setNullableString(i++, order.price)
        preparedStatementOrderHistory.setInt(i++, order.status)
        preparedStatementOrderHistory.setNullableString(i++, order.rejectReason)
        preparedStatementOrderHistory.setTimestamp(i++, order.statusDate.toSqlTimestamp())
        preparedStatementOrderHistory.setTimestamp(i++, order.createdAt.toSqlTimestamp())
        preparedStatementOrderHistory.setTimestamp(i++, order.registered.toSqlTimestamp())
        if (order.hasLastMatchTime()) preparedStatementOrderHistory.setNullableTimestamp(i++, order.lastMatchTime) else preparedStatementOrderHistory.setNull(i++, Types.TIMESTAMP)
        preparedStatementOrderHistory.setNullableString(i++, order.lowerLimitPrice)
        preparedStatementOrderHistory.setNullableString(i++, order.lowerPrice)
        preparedStatementOrderHistory.setNullableString(i++, order.upperLimitPrice)
        preparedStatementOrderHistory.setNullableString(i++, order.upperPrice)
        preparedStatementOrderHistory.setInt(i++, order.timeInForce)
        if (order.hasExpiryTime()) preparedStatementOrderHistory.setNullableTimestamp(i++, order.expiryTime) else preparedStatementOrderHistory.setNull(i++, Types.TIMESTAMP)
        preparedStatementOrderHistory.setNullableString(i++, order.parentExternalId)
        preparedStatementOrderHistory.setNullableString(i, order.childExternalId)

        val rs = preparedStatementOrderHistory.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get order history id")
    }
}