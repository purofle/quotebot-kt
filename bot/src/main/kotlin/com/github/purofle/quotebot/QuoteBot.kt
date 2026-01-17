package com.github.purofle.quotebot

import com.github.purofle.quotebot.handler.QuoteCommandHandler
import com.github.purofle.quotebot.service.AvatarDownloader
import com.github.purofle.quotebot.service.QuoteMessageService
import com.github.purofle.quotebot.service.QuoteStickerService
import com.github.purofle.quotebot.tdlibhelper.TdLibBot
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.GetMe
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.generics.TelegramClient

private val logger = KotlinLogging.logger {}

class QuoteBot(
    botToken: String,
    tdLibBot: TdLibBot,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : LongPollingSingleThreadUpdateConsumer {

    private val telegramClient: TelegramClient = OkHttpTelegramClient(botToken)
    private val httpClient: OkHttpClient = OkHttpClient()

    // Lazy initialization to avoid blocking in constructor
    private val botUser: User by lazy {
        runBlocking(Dispatchers.IO) {
            telegramClient.execute(GetMe())
        }.also { logger.info { "Bot started as @${it.userName}" } }
    }

    // Initialize services
    private val avatarDownloader = AvatarDownloader(botToken, telegramClient, httpClient)
    private val messageService = QuoteMessageService(tdLibBot, avatarDownloader)
    private val stickerService = QuoteStickerService(telegramClient)
    private val quoteCommandHandler = QuoteCommandHandler(messageService, stickerService)

    override fun consume(update: Update) {
        if (!update.hasMessage()) return
        if (update.message.from?.id == botUser.id) return
        if (!update.message.isCommand) return

        scope.launch {
            try {
                handleCommand(update)
            } catch (t: Throwable) {
                logger.error(t) { "handleCommand failed" }
            }
        }
    }

    private suspend fun handleCommand(update: Update) {
        val text = update.message.text ?: return

        when {
            text.startsWith("/q") -> quoteCommandHandler.handle(update)
        }
    }
}
