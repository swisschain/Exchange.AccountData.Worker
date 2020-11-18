package com.me.history.writer

import com.me.history.writer.utils.toSqlTimestamp
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages
import removeExtraWhitespaces
import java.sql.Connection

class MessagePersister (connection: Connection) {

    private val INSERT_MESSAGE = """
        INSERT INTO messages (message_type, sequence_number, message_id, request_id, version, created_at, event_type)
        VALUES               (?           , ?              , ?         , ?         , ?      , ?         , ?) 
        RETURNING id;
    """.removeExtraWhitespaces().trimIndent()

    private val preparedStatement = connection.prepareStatement(INSERT_MESSAGE)

    fun persistMessage(message: OutgoingMessages.Header): Long {
        var i  = 1
        preparedStatement.setInt(i++, message.messageType)
        preparedStatement.setLong(i++, message.sequenceNumber)
        preparedStatement.setString(i++, message.messageId)
        preparedStatement.setString(i++, message.requestId)
        preparedStatement.setString(i++, message.version)
        preparedStatement.setTimestamp(i++, message.timestamp.toSqlTimestamp())
        preparedStatement.setString(i, message.eventType)
        val rs = preparedStatement.executeQuery()
        if (rs.next()) {
            return rs.getLong(1)
        }
        throw Exception("Unable to get message id")
    }
}