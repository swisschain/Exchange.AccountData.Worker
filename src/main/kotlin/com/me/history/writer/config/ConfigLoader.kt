package com.me.history.writer.config

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

object ConfigLoader {
    private val logger = LogManager.getLogger()
    fun loadConfig(): Config? {
        try {
            val url = System.getenv("WORKER_CONFIG")
            logger.info("Loading remote config from $url")
            val inputStream = BufferedReader(InputStreamReader(URL(url).openConnection().getInputStream()))
            val response = StringBuilder()
            var inputLine = inputStream.readLine()

            while (inputLine != null) {
                response.append(inputLine)
                inputLine = inputStream.readLine()
            }

            inputStream.close()

            val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create()
            return gson.fromJson(response.toString(), Config::class.java)
        } catch (e: IOException) {
            logger.error("Unable to load remote config file due to " + e.message, e)
        }
        return null
    }
}