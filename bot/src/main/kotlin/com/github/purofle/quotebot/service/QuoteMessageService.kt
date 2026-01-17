package com.github.purofle.quotebot.service

import com.github.purofle.quotebot.tdlibhelper.QuoteUser
import com.github.purofle.quotebot.tdlibhelper.TdLibBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.drinkless.tdlib.TdApi

private val logger = KotlinLogging.logger {}

class QuoteMessageService(
    private val tdLibBot: TdLibBot,
    private val avatarDownloader: AvatarDownloader
) {
    suspend fun fetchMessages(chatId: Long, startMessageId: Int, count: Int): List<TdApi.Message> {
        val messageIds = LongArray(count) { idx ->
            val messageId = startMessageId + idx
            // actualMessageId = messageId * 2^20 = messageId << 20
            (messageId.toLong() shl 20)
        }

        return tdLibBot.getMessages(chatId = chatId, messageIds = messageIds).messages.filterNotNull()
    }

    suspend fun fetchQuoteUsers(messages: List<TdApi.Message>): List<QuoteUser> {
        return messages
            .mapNotNull { it.forwardInfo?.origin as? TdApi.MessageOriginUser }
            .map { tdLibBot.getUser(it.senderUserId) }
            .map { user ->
                val avatar = user.profilePhoto?.big?.remote?.id?.let { fileId ->
                    avatarDownloader.downloadAvatar(fileId)
                }

                QuoteUser(
                    id = user.id,
                    fullName = listOfNotNull(user.firstName, user.lastName).joinToString(" "),
                    avatar = avatar
                )
            }
    }
}

