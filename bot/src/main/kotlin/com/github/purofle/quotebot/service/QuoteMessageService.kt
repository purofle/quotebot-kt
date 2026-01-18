package com.github.purofle.quotebot.service

import com.github.purofle.quotebot.tdlibhelper.QuoteUser
import com.github.purofle.quotebot.tdlibhelper.TdLibBot
import org.drinkless.tdlib.TdApi

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
        return messages.mapNotNull { msg ->
            val origin = msg.forwardInfo?.origin ?: return@mapNotNull null

            when (origin) {
                is TdApi.MessageOriginUser -> {
                    val user = tdLibBot.getUser(origin.senderUserId)
                    val avatar = avatarDownloader.downloadAvatar(user.id)

                    QuoteUser(
                        id = user.id,
                        fullName = listOfNotNull(user.firstName, user.lastName).joinToString(" "),
                        avatar = avatar
                    )
                }

                is TdApi.MessageOriginHiddenUser -> {
                    QuoteUser(
                        id = -1L,
                        fullName = origin.senderName,
                        avatar = null
                    )
                }

                else -> null
            }
        }
    }

}

