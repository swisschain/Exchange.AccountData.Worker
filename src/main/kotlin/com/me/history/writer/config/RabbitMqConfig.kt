package com.me.history.writer.config

data class RabbitMqConfig(
        val uri: String,
        val exchange: String,
        val queue: String,
        val name: String,
        val connectionTryInterval: Long
)