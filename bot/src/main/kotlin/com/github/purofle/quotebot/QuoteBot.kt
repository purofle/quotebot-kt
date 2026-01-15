package com.github.purofle.quotebot

import com.github.purofle.quotebot.tdlibhelper.TdLibBot
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.drinkless.tdlib.TdApi
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.GetMe
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.ReplyParameters
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.generics.TelegramClient

private val logger = KotlinLogging.logger {}

class QuoteBot(
    botToken: String,
    private val tdLibBot: TdLibBot,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : LongPollingSingleThreadUpdateConsumer {

    private val telegramClient: TelegramClient = OkHttpTelegramClient(botToken)
    private val botUser: User = runBlocking(Dispatchers.IO) {
        telegramClient.execute(GetMe())
    }.also { logger.info { "Bot started as @${it.userName}" } }

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
            text.startsWith("/q") -> {
                // 例如：/q 3
                var limit = Regex("""^/q\s+(\d+)\s*""")
                    .find(text)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.toIntOrNull()

                if (limit == null) limit = 1

                val messageIds = mutableListOf<Long>()

                val replyMessageId = update.message.replyToMessage.messageId
                for (i in replyMessageId until replyMessageId + limit) {
                    val actualMessageId = (i shl 20).toLong()
                    messageIds += actualMessageId
                }

                val messages = tdLibBot.getMessages(
                    chatId = update.message.chatId,
                    messageIds = messageIds.toLongArray()
                ).messages
                    .filterNot { it == null }
                    .map { (it.content as TdApi.MessageText).text }

                val msg = SendMessage.builder()
                    .chatId(update.message.chatId)
                    .text("你再Q？${messages.joinToString("\n")}")
                    .replyParameters(
                        ReplyParameters.builder()
                            .chatId(update.message.chatId)
                            .messageId(update.message.messageId)
                            .build()
                    )
                    .build()

                withContext(Dispatchers.IO) {
                    telegramClient.execute(msg)
                }
            }
        }
    }
}
