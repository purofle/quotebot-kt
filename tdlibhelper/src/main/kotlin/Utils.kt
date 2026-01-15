package com.github.purofle.quotebot.tdlibhelper

import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T : TdApi.Object> Client.sendAwait(query: TdApi.Function<T>): T {
    return suspendCancellableCoroutine { cont ->
        send(query) { obj ->
            when (obj) {
                is TdApi.Error -> cont.resumeWithException(RuntimeException("TDLib error ${obj.code}: ${obj.message}"))
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    cont.resume(obj as T)
                }
            }
        }
    }
}