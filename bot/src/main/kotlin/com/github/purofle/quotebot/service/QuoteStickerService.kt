package com.github.purofle.quotebot.service

import com.github.purofle.quotebot.render.QuoteDraw
import com.github.purofle.quotebot.tdlibhelper.QuoteUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.ReplyParameters
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.ByteArrayInputStream


class QuoteStickerService(
    private val telegramClient: TelegramClient,
    private val fontPath: String = "C:\\Windows\\Fonts\\HarmonyOS_Sans_SC_Regular.ttf"
) {
    suspend fun generateAndSendQuoteSticker(
        messages: List<Pair<QuoteUser, TdApi.Message>>,
        chatId: Long,
        replyToMessageId: Int
    ) {
        val photo = QuoteDraw(messages, fontPath).encodeWebp().bytes

        val sendMessage = SendSticker.builder()
            .chatId(chatId)
            .sticker(InputFile(ByteArrayInputStream(photo), "quote.webp"))
            .replyParameters(
                ReplyParameters.builder()
                    .chatId(chatId)
                    .messageId(replyToMessageId)
                    .build()
            )
            .build()

        withContext(Dispatchers.IO) {
            telegramClient.execute(sendMessage)
        }
    }
}

