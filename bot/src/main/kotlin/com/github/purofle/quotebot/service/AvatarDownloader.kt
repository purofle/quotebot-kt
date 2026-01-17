package com.github.purofle.quotebot.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.generics.TelegramClient

private val logger = KotlinLogging.logger {}

class AvatarDownloader(
    private val botToken: String,
    private val telegramClient: TelegramClient,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    suspend fun downloadAvatar(fileId: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = telegramClient.execute(GetFile(fileId))
            val path = file.filePath ?: return@withContext null

            val request = Request.Builder()
                .url("https://api.telegram.org/file/bot$botToken/$path")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.warn { "Failed to download avatar: ${response.code}" }
                    null
                } else {
                    response.body?.bytes()
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error downloading avatar" }
            null
        }
    }
}

