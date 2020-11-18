package com.me.history.writer.config

data class Config(
        val dbConfig: DatabaseConfig,
        val rabbitMqConfig: RabbitMqConfig,
        val isAlivePort: Int
)