package com.me.history.writer

import com.me.history.writer.config.ConfigLoader
import com.me.history.writer.rabbitmq.sub.RabbitMqSubscriber
import com.me.isalive.IsAliveService
import org.apache.logging.log4j.LogManager
import org.flywaydb.core.Flyway

class AppStarter: Runnable {
    companion object {
        private val LOGGER = LogManager.getLogger(AppStarter::class.java.name)
    }

    override fun run() {
        val config = ConfigLoader.loadConfig()

        if (config == null) {
            LOGGER.error("Can not load config")
            return
        }

        if (!migrateDatabase(config.dbConfig.url, config.dbConfig.user, config.dbConfig.password)) {
            LOGGER.error("Unable to start app")
            return
        } else {
            LOGGER.info("Database[${config.dbConfig.url}] is Up to Date")
        }

        RabbitMqSubscriber(config.rabbitMqConfig, "HistoryWriter", "1.0", config.dbConfig).start()

        IsAliveService(config.isAlivePort)

        while (true) {
            Thread.sleep(60000)
        }
    }

    private fun migrateDatabase(jdbcUrl: String, jdbcUser: String, jdbcPassword: String): Boolean {
        try {
            Flyway.configure()
                    .dataSource(jdbcUrl, jdbcUser, jdbcPassword)
                    .load()
                    .migrate()
            return true
        } catch (e: Exception) {
            LOGGER.error("Unable to migrate database: ${e.message}", e)
        }
        return false
    }
}

fun main() {
    Thread(AppStarter()).start()
}
