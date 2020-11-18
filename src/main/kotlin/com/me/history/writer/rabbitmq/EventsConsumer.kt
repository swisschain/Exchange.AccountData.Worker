package com.me.history.writer.rabbitmq

import com.me.history.writer.DatabasePersister
import com.me.history.writer.config.DatabaseConfig
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownSignalException
import com.swisschain.matching.engine.messages.outgoing.OutgoingMessages.*
import org.apache.logging.log4j.LogManager

class EventsConsumer(
        private val name: String,
        private val channel: Channel,
        private val jdbcConfig: DatabaseConfig) : Consumer {

    companion object {
        private val LOGGER = LogManager.getLogger(EventsConsumer::class.java.name)
    }

    private val databasePersister = DatabasePersister(jdbcConfig)

    private var consumerTag: String? = null

    override fun handleConsumeOk(consumerTag: String?) {
        LOGGER.info("RabbitMQConsumer[$name, ${this.consumerTag}]: registered with tag: $consumerTag")
        this.consumerTag = consumerTag
    }

    override fun handleCancelOk(consumerTag: String?) {
        LOGGER.info("RabbitMQConsumer[$name, ${this.consumerTag}]: canceled by channel with tag: $consumerTag")
        this.consumerTag = consumerTag
    }

    override fun handleCancel(consumerTag: String?) {
        LOGGER.info("RabbitMQConsumer[$name, ${this.consumerTag}]: canceled not by channel with tag: $consumerTag")
        this.consumerTag = consumerTag
    }

    override fun handleRecoverOk(consumerTag: String?) {
        LOGGER.info("RabbitMQConsumer[$name, ${this.consumerTag}]: recovered with tag: $consumerTag")
        this.consumerTag = consumerTag
    }

    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
        LOGGER.info("RabbitMQConsumer[$name, ${this.consumerTag}]: channel/connection shutdown: ${sig?.message} with tag: $consumerTag")
    }

    override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
        val headers = properties.headers
        val messageType = MessageType.forNumber(headers["MessageType"] as Int)
        val eventType = headers["EventType"]
        LOGGER.debug("RabbitMQConsumer[$name, ${this.consumerTag}]: received message [${messageType.name}], event [$eventType]")
        if (databasePersister.persistMessage(messageType, body)) {
            LOGGER.debug("RabbitMQConsumer[$name, ${this.consumerTag}]: persisted message [${messageType.name}], event [$eventType]")
            channel.basicAck(envelope.deliveryTag, false)
        } else {
            LOGGER.debug("RabbitMQConsumer[$name, ${this.consumerTag}]: unable to persist message [${messageType.name}], event [$eventType]")
            channel.basicReject(envelope.deliveryTag, true)
        }
    }
}