package com.me.history.writer

import com.me.history.writer.utils.setNullableInt
import com.me.history.writer.utils.setNullableLong
import com.me.history.writer.utils.setNullableString
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.FeeTransfer
import removeExtraWhitespaces
import java.sql.Connection

class FeeTransfersPersister (connection: Connection) {

    private val INSERT_FEE_TRANSFER_MESSAGE = """
        INSERT INTO fee_transfers (message_id, sequence_number, broker_id, volume,  source_account_id, source_wallet_id, target_account_id, target_wallet_id, asset_id, fee_coef, index, order_id, trade_id, cash_in_id, cash_out_id, cash_transfer_id)
        VALUES                    (?         , ?              , ?        , ?      , ?                , ?               , ?                , ?               , ?       , ?       , ?    , ?       , ?       , ?         , ?          , ?)        
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatement = connection.prepareStatement(INSERT_FEE_TRANSFER_MESSAGE)

    fun persistTradeFeeTransfer(messageId: Long, sequenceId: Long, brokerId: String, feeTransfer: FeeTransfer, orderId: Long, tradeId: Long) {
        persistFeeTransfer(messageId, sequenceId, brokerId, feeTransfer, orderId, tradeId, null, null, null)
    }

    fun persistCashInFeeTransfer(messageId: Long, sequenceId: Long, brokerId: String, feeTransfer: FeeTransfer, cashInId: Long) {
        persistFeeTransfer(messageId, sequenceId, brokerId, feeTransfer, null, null, cashInId, null, null)
    }

    fun persistCashOutFeeTransfer(messageId: Long, sequenceId: Long, brokerId: String, feeTransfer: FeeTransfer, cashOutId: Long) {
        persistFeeTransfer(messageId, sequenceId, brokerId, feeTransfer, null, null, null, cashOutId, null)
    }

    fun persistCashTransferFeeTransfer(messageId: Long, sequenceId: Long, brokerId: String, feeTransfer: FeeTransfer, cashTransferId: Long) {
        persistFeeTransfer(messageId, sequenceId, brokerId, feeTransfer, null, null, null, null, cashTransferId)
    }

    private fun persistFeeTransfer(messageId: Long, sequenceId: Long, brokerId: String, feeTransfer: FeeTransfer, orderId: Long?, tradeId: Long?, cashInId: Long?, cashOutId: Long?, cashTransferId: Long?) {
        var i = 1
        preparedStatement.setLong(i++, messageId)
        preparedStatement.setLong(i++, sequenceId)
        preparedStatement.setString(i++, brokerId)
        preparedStatement.setString(i++, feeTransfer.volume)
        preparedStatement.setLong(i++, feeTransfer.sourceAccountId)
        preparedStatement.setLong(i++, feeTransfer.sourceWalletId)
        preparedStatement.setLong(i++, feeTransfer.targetAccountId)
        preparedStatement.setLong(i++, feeTransfer.targetWalletId)
        preparedStatement.setString(i++, feeTransfer.assetId)
        preparedStatement.setNullableString(i++, feeTransfer.feeCoef)
        preparedStatement.setNullableInt(i++, feeTransfer.index)
        preparedStatement.setNullableLong(i++, orderId)
        preparedStatement.setNullableLong(i++, tradeId)
        preparedStatement.setNullableLong(i++, cashInId)
        preparedStatement.setNullableLong(i++, cashOutId)
        preparedStatement.setNullableLong(i, cashTransferId)

        preparedStatement.executeUpdate()
    }
}