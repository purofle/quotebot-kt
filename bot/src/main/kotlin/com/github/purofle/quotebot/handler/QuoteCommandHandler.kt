package com.github.purofle.quotebot.handler

import com.github.purofle.quotebot.service.QuoteMessageService
import com.github.purofle.quotebot.service.QuoteStickerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.objects.Update

private val logger = KotlinLogging.logger {}

class QuoteCommandHandler(
    private val quoteMessageService: QuoteMessageService,
    private val quoteStickerService: QuoteStickerService
) {
    private val commandRegex = Regex("""^/q\s+(\d+)\s*""")

    suspend fun handle(update: Update) {
        val text = update.message.text ?: return
        val replyToMessage = update.message.replyToMessage ?: run {
            logger.warn { "Quote command requires a reply to message" }
            return
        }

        val limit = parseLimit(text)
        val messages = quoteMessageService.fetchMessages(
            chatId = update.message.chatId,
            startMessageId = replyToMessage.messageId,
            count = limit
        )

        if (messages.isEmpty()) {
            logger.warn { "No messages found to quote" }
            return
        }

        // Fetch quote users (if needed for your render logic)
        val quoteUsers = quoteMessageService.fetchQuoteUsers(messages)
        logger.debug { "Fetched ${quoteUsers.size} quote users" }

        quoteStickerService.generateAndSendQuoteSticker(
            messages = quoteUsers.zip(messages),
            chatId = update.message.chatId,
            replyToMessageId = update.message.messageId
        )
    }

    private fun parseLimit(text: String): Int {
        return commandRegex.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 1
    }
}

