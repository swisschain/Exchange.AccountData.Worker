package com.me.history.writer.rabbitmq.sub

import com.me.history.writer.config.DatabaseConfig
import com.me.history.writer.rabbitmq.EventsConsumer
import com.me.history.writer.config.RabbitMqConfig
import com.rabbitmq.client.BuiltinExchangeType.FANOUT
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import org.apache.logging.log4j.LogManager
import java.io.Closeable
import java.util.concurrent.TimeUnit

class RabbitMqSubscriber(
        private val config: RabbitMqConfig,
        private val appName: String,
        private val appVersion: String,
        private val jdbcConfig: DatabaseConfig
) : Closeable, Thread() {
    companion object {
        private val LOGGER = LogManager.getLogger(RabbitMqSubscriber::class.java.name)
        private const val CONNECTION_NAME_FORMAT = "[Sub][%s] %s %s to %s:%s"
    }

    private var channel: Channel? = null

    override fun run() {
        while (!connect()) {
            sleep(config.connectionTryInterval)
        }
        startClient()
    }

    private fun connect(): Boolean {
        val factory = ConnectionFactory()
        factory.setUri(config.uri)
        factory.requestedHeartbeat =  TimeUnit.MILLISECONDS.toSeconds(1000L).toInt()
        factory.handshakeTimeout = 1000
        factory.connectionTimeout = 1000
        factory.isAutomaticRecoveryEnabled = true

        val logSuffix = "${factory.host}:${factory.port}, exchange: ${config.exchange}, queue: ${config.queue}"

        LOGGER.info("Connecting to RabbitMQ: $logSuffix")

        return try {
            val connection = factory.newConnection(CONNECTION_NAME_FORMAT.format(config.name, appName, appVersion, config.exchange, config.queue))
            channel = connection.createChannel()
            channel!!.basicQos(10, true)
            channel!!.exchangeDeclare(config.exchange, FANOUT, true)
            LOGGER.info("Connected to RabbitMQ: $logSuffix")
            true
        } catch (e: Exception) {
            LOGGER.error("Unable to connect to RabbitMQ: $logSuffix: ${e.message}", e)
            false
        }
    }

    private fun startClient() {
        channel!!.basicConsume(config.queue, false, EventsConsumer("EventsConsumer", channel!!, jdbcConfig))
    }

    override fun close() {
        this.channel?.connection?.close()
    }
}