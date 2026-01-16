package com.github.purofle.quotebot.render

import org.drinkless.tdlib.TdApi
import org.jetbrains.skia.*

class QuoteDraw(private val messages: List<TdApi.Message>, font: String) {

    val scale = 1f

    val avatarSize = 50f * scale

    val padding = 8f * scale

    val fontsize = 18f * scale

    val maxWidth = 512f * scale

    val font = Font(FontMgr.default.makeFromFile(font)!!, fontsize)

    lateinit var canvas: Canvas

    private val texts: List<String> =
        messages
            .mapNotNull { it.content as? TdApi.MessageText }
            .map { it.text.text }
            .filter { it.isNotBlank() }

    private fun drawAvatar(x: Float, y: Float, size: Float, message: TdApi.Message) {
        val centerX = (x + size) / 2f
        val centerY = (y + size) / 2f

        val paint = Paint().apply {
            color = Color.makeRGB(40, 150, 172)
            isAntiAlias = true
        }

        canvas.drawCircle(centerX, centerY, size / 2, paint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }

        val ch = getSenderName(message)?.firstOrNull()?.toString() ?: "?"

        val fm = font.measureText(ch)
        val textX = centerX - fm.width / 2
        val textY = centerY + fm.height / 2
        val line = TextLine.make(ch, font)
        canvas.drawTextLine(line, textX, textY, textPaint)
    }

    private fun getSenderName(message: TdApi.Message): String? {
        val origin = message.forwardInfo?.origin
        if (origin is TdApi.MessageOriginHiddenUser) {
            val senderName = origin.senderName
            return senderName
        }
        return null
    }

    private fun drawDialog(bubbleX: Float, bubbleY: Float, message: TdApi.Message) {
        val sender = getSenderName(message)
        val senderTextSize = font.measureText(sender)
        val textSize = font.measureText((message.content as TdApi.MessageText).text.text)

        val bubbleW = maxOf(senderTextSize.width, textSize.width) + padding * 2
        val bubbleH = senderTextSize.height + textSize.height + padding * 3

        val radius = 8f

        val backgroundPaint = Paint().apply {
            color = Color.makeRGB(26, 20, 41)
            isAntiAlias = true
        }

        val bubble = RRect.makeXYWH(bubbleX, bubbleY, bubbleW, bubbleH, radius, radius)
        canvas.drawRRect(bubble, backgroundPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }

        val senderLine = TextLine.make(sender ?: "", font)
        val senderX = bubbleX + padding
        val senderY = bubbleY + padding / 2 + senderTextSize.height
        canvas.drawTextLine(senderLine, senderX, senderY, textPaint)

        val messageLine = TextLine.make((message.content as TdApi.MessageText).text.text, font)
        val messageX = bubbleX + padding
        val messageY = senderY + padding + textSize.height
        canvas.drawTextLine(messageLine, messageX, messageY, textPaint)
    }

    fun measureFullSize(): Pair<Float, Float> {
        val totalHeight = avatarSize
        val totalWidth = avatarSize
        return Pair(totalWidth, totalHeight)
    }

    fun render() {
        canvas.clear(Color.TRANSPARENT)

        drawAvatar(0f * scale, 0f * scale, avatarSize, messages[0])
        drawDialog(avatarSize + padding * 2, 0f, messages[0])
    }

    fun encodeWebp(quality: Int = 100): Data {
//        val size = measureFullSize()
//        val info = ImageInfo.makeN32Premul(size.first.toInt(), size.second.toInt())
        val info = ImageInfo.makeN32Premul(512, 1000)
//        println("size: height=${size.second} width=${size.first}")
        Surface.makeRaster(info).use { surface ->

            canvas = surface.canvas

            render()

            surface.flushAndSubmit()
            surface.makeImageSnapshot().use { img ->
                return img.encodeToData(EncodedImageFormat.WEBP, quality)
                    ?: error("encoded data failed")
            }
        }
    }
}
