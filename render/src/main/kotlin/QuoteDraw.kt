package com.github.purofle.quotebot.render

import org.drinkless.tdlib.TdApi
import org.jetbrains.skia.*

class QuoteDraw(private val messages: List<TdApi.Message>, fontFile: String) {

    val scale = 1f

    val avatarSize = 50f * scale

    val padding = 8f * scale

    val fontsize = 18f * scale

    val font: Font = Font(FontMgr.default.makeFromFile(fontFile)!!, fontsize)

    lateinit var canvas: Canvas

    init {
        font.edging = FontEdging.ANTI_ALIAS
        font.isSubpixel = true
    }

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
        when (origin) {
            is TdApi.MessageOriginHiddenUser -> {
                return origin.senderName
            }

            is TdApi.MessageOriginUser -> {
                return origin.senderUserId.toString()
            }
        }
        return null
    }

    private fun drawDialog(bubbleX: Float, bubbleY: Float, message: TdApi.Message) {
        val sender = getSenderName(message) ?: ""
        val text = (message.content as TdApi.MessageText).text.text

        val (bubbleW, bubbleH) = measureDialogSize(message)
        val textBounds = font.measureText(text)
        val senderBounds = font.measureText(sender)

        val radius = 12f
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

        val senderLine = TextLine.make(sender, font)
        val senderX = bubbleX + padding * 2
        val senderTopY = bubbleY + padding * 2
        val senderBaselineY = senderTopY - senderBounds.top
        canvas.drawTextLine(senderLine, senderX, senderBaselineY, textPaint)

        val messageLine = TextLine.make(text, font)
        val messageX = bubbleX + padding * 2
        val messageTopY = senderTopY + senderBounds.height + padding * 1
        val messageBaselineY = messageTopY - textBounds.top
        canvas.drawTextLine(messageLine, messageX, messageBaselineY, textPaint)
    }


    fun measureDialogSize(message: TdApi.Message): Pair<Float, Float> {
        val sender = getSenderName(message)
        val senderTextSize = font.measureText(sender)
        val textSize = font.measureText((message.content as TdApi.MessageText).text.text)

        val bubbleW = maxOf(senderTextSize.width, textSize.width) + padding * (2 + 2) // leftPadding + rightPadding
        val bubbleH =
            senderTextSize.height + textSize.height + padding * (2 + 1 + 2) // topPadding + between + bottomPadding

        return Pair(bubbleW, bubbleH)
    }

    fun measureFullSize(): Pair<Float, Float> {
        val totalHeight = maxOf(
            measureDialogSize(messages[0]).second,
            avatarSize
        )
        val totalWidth = avatarSize + padding + measureDialogSize(messages[0]).first
        return Pair(totalWidth, totalHeight)
    }

    fun render() {
        canvas.clear(Color.TRANSPARENT)

        drawAvatar(0f * scale, 0f * scale, avatarSize, messages[0])
        drawDialog(avatarSize + padding, 0f, messages[0])
    }

    fun encodeWebp(quality: Int = 100): Data {
        val size = measureFullSize()
        val info = ImageInfo.makeN32Premul(size.first.toInt(), size.second.toInt())
//        val info = ImageInfo.makeN32Premul(512, 1000)
        println("size: height=${size.second} width=${size.first}")
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
