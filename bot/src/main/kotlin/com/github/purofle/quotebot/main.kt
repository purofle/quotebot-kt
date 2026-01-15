package com.github.purofle.quotebot

import com.github.purofle.quotebot.tdlibhelper.TdLibBot
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

fun main(): Unit = runBlocking {
    val token = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: error("TELEGRAM_BOT_TOKEN environment variable is not set")

    val apiId = System.getenv("TELEGRAM_API_ID")?.toIntOrNull()
        ?: error("TELEGRAM_API_ID environment variable is not set or is not a valid integer")

    val apiHash = System.getenv("TELEGRAM_API_HASH")
        ?: error("TELEGRAM_API_HASH environment variable is not set")

    TelegramBotsLongPollingApplication().use { botApp ->

        val td = TdLibBot(
            botToken = token,
            apiId = apiId,
            apiHash = apiHash,
        )

        val tdJob = launch {
            try {
                td.connect()
            } catch (t: Throwable) {
                println("TDLib connect crashed:")
                t.printStackTrace()
                throw t
            }
        }

        botApp.registerBot(token, QuoteBot(token, td))

        tdJob.join()
    }
}
