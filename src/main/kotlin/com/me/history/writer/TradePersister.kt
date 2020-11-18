package com.me.history.writer

import com.me.history.writer.utils.toSqlTimestamp
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.*
import removeExtraWhitespaces
import java.sql.Connection

class TradePersister (connection: Connection) {
    private val INSERT_TRADE = """
        INSERT INTO trades (order_id, order_history_id, message_id, sequence_number, external_order_id, trade_id, broker_id, account_id, wallet_id, base_asset_id, base_volume, price, timestamp, opposite_order_id, opposite_external_order_id, opposite_wallet_id, quoting_asset_id, quoting_volume, index, absolute_spread, relative_spread, role)
        VALUES             (?       , ?               , ?         , ?              , ?                , ?       , ?        , ?         , ?        , ?            , ?          , ?    , ?        , ?                , ?                         , ?                 , ?               , ?             , ?    , ?              , ?              , ?)
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatementTrade = connection.prepareStatement(INSERT_TRADE)

    private val feeTransfersPersister = FeeTransfersPersister(connection)

    fun persistTrades(messageId: Long, sequenceNumber: Long, orderId: Long, externalOrderId: String, orderHistoryId: Long, brokerId: String, accountId: Long, walletId: Long, trades: List<ExecutionEvent.Order.Trade>) {
        trades.forEach {
            val tradeId = persistTrade(messageId, sequenceNumber, orderId, externalOrderId, orderHistoryId, brokerId, accountId, walletId, it)
            it.feesList.forEach { feeTransfer ->
                feeTransfersPersister.persistTradeFeeTransfer(messageId, sequenceNumber, brokerId, feeTransfer, orderId, tradeId)
            }
        }
    }

    private fun persistTrade(messageId: Long, sequenceNumber: Long, orderId: Long, externalOrderId: String, orderHistoryId: Long, brokerId: String, accountId: Long, walletId: Long, trade: ExecutionEvent.Order.Trade): Long {
        var i = 1
        preparedStatementTrade.setLong(i++, orderId)
        preparedStatementTrade.setLong(i++, orderHistoryId)
        preparedStatementTrade.setLong(i++, messageId)
        preparedStatementTrade.setLong(i++, sequenceNumber)
        preparedStatementTrade.setString(i++, externalOrderId)
        preparedStatementTrade.setString(i++, trade.tradeId)
        preparedStatementTrade.setString(i++, brokerId)
        preparedStatementTrade.setLong(i++, accountId)
        preparedStatementTrade.setLong(i++, walletId)
        preparedStatementTrade.setString(i++, trade.baseAssetId)
        preparedStatementTrade.setString(i++, trade.baseVolume)
        preparedStatementTrade.setString(i++, trade.price)
        preparedStatementTrade.setTimestamp(i++, trade.timestamp.toSqlTimestamp())
        preparedStatementTrade.setString(i++, trade.oppositeOrderId)
        preparedStatementTrade.setString(i++, trade.oppositeExternalOrderId)
        preparedStatementTrade.setLong(i++, trade.oppositeWalletId)
        preparedStatementTrade.setString(i++, trade.quotingAssetId)
        preparedStatementTrade.setString(i++, trade.quotingVolume)
        preparedStatementTrade.setInt(i++, trade.index)
        preparedStatementTrade.setString(i++, trade.absoluteSpread)
        preparedStatementTrade.setString(i++, trade.relativeSpread)
        preparedStatementTrade.setInt(i, trade.role)

        val rs = preparedStatementTrade.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get trade id")
    }
}