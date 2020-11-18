package com.me.history.writer

import com.me.history.writer.utils.setNullableInt
import com.me.history.writer.utils.setNullableLong
import com.me.history.writer.utils.setNullableString
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.FeeInstruction
import removeExtraWhitespaces
import java.sql.Connection

class FeeInstructionsPersister (connection: Connection) {

    private val INSERT_FEE_INSTRUCTION_MESSAGE = """
        INSERT INTO fee_instructions (message_id, sequence_number, broker_id, fee_type, size, size_type, maker_size, maker_size_type, source_account_id, source_wallet_id, target_account_id, target_wallet_id, assets_ids, maker_fee_modificator, index, order_id, cash_in_id, cash_out_id, cash_transfer_id)
        VALUES                       (?         , ?              , ?        , ?       , ?   , ?        , ?         , ?              , ?                , ?               , ?                , ?               , ?         , ?                    , ?    , ?       , ?         , ?          , ?)
        
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatement = connection.prepareStatement(INSERT_FEE_INSTRUCTION_MESSAGE)

    fun persistOrderFeeInstruction(messageId: Long, sequenceId: Long, brokerId: String, feeInstruction: FeeInstruction, orderId: Long) {
        persistFeeInstruction(messageId, sequenceId, brokerId, feeInstruction, orderId, null, null, null)
    }

    fun persistCashInFeeInstruction(messageId: Long, sequenceId: Long, brokerId: String, feeInstruction: FeeInstruction, cashInId: Long) {
        persistFeeInstruction(messageId, sequenceId, brokerId, feeInstruction, null, cashInId, null, null)
    }

    fun persistCashOutFeeInstruction(messageId: Long, sequenceId: Long, brokerId: String, feeInstruction: FeeInstruction, cashOutId: Long) {
        persistFeeInstruction(messageId, sequenceId, brokerId, feeInstruction, null, null, cashOutId, null)
    }

    fun persistCashTransferFeeInstruction(messageId: Long, sequenceId: Long, brokerId: String, feeInstruction: FeeInstruction, cashTransferId: Long) {
        persistFeeInstruction(messageId, sequenceId, brokerId, feeInstruction, null, null, null, cashTransferId)
    }

    private fun persistFeeInstruction(messageId: Long, sequenceId: Long, brokerId: String, feeInstruction: FeeInstruction, orderId: Long?, cashInId: Long?, cashOutId: Long?, cashTranferId: Long?) {
        var i = 1
        preparedStatement.setLong(i++, messageId)
        preparedStatement.setLong(i++, sequenceId)
        preparedStatement.setString(i++, brokerId)
        preparedStatement.setInt(i++, feeInstruction.type)
        preparedStatement.setString(i++, feeInstruction.size)
        preparedStatement.setNullableInt(i++, feeInstruction.sizeType)
        preparedStatement.setNullableString(i++, feeInstruction.makerSize)
        preparedStatement.setNullableInt(i++, feeInstruction.makerSizeType)
        preparedStatement.setNullableLong(i++, feeInstruction.sourceAccountId)
        preparedStatement.setNullableLong(i++, feeInstruction.sourceWalletId)
        preparedStatement.setNullableLong(i++, feeInstruction.targetAccountId)
        preparedStatement.setNullableLong(i++, feeInstruction.targetWalletId)
        preparedStatement.setNullableString(i++, if (feeInstruction.assetsIdsCount > 0) feeInstruction.assetsIdsList.joinToString (separator = ",") else null)
        preparedStatement.setNullableString(i++, feeInstruction.makerFeeModificator)
        preparedStatement.setNullableInt(i++, feeInstruction.index)
        preparedStatement.setNullableLong(i++, orderId)
        preparedStatement.setNullableLong(i++, cashInId)
        preparedStatement.setNullableLong(i++, cashOutId)
        preparedStatement.setNullableLong(i, cashTranferId)

        preparedStatement.executeUpdate()
    }
}