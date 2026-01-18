package com.github.purofle.quotebot.render

import com.github.purofle.quotebot.tdlibhelper.QuoteUser
import org.drinkless.tdlib.TdApi
import org.jetbrains.skia.*

class QuoteDraw(private val messages: Map<QuoteUser, TdApi.Message>, fontFile: String) {

    val scale = 2f

    val avatarSize = 50f * scale

    val padding = 8f * scale

    val fontsize = 18f * scale

    val font: Font = Font(FontMgr.default.makeFromFile(fontFile)!!, fontsize)

    lateinit var canvas: Canvas

    init {
        font.edging = FontEdging.ANTI_ALIAS
        font.isSubpixel = true
    }

    private fun drawAvatar(y: Float, size: Float, user: QuoteUser) {
        val centerX = size / 2f
        val centerY = y + size / 2f
        val r = size / 2f

        // 先画圆底色（当图片透明/解码失败时也不会空）
        val bgPaint = Paint().apply {
            color = Color.makeRGB(40, 150, 172)
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, r, bgPaint)

        val bytes = user.avatar
        if (bytes != null) {
            val img = Image.makeFromEncoded(bytes)

            val saveCount = canvas.save()
            val clipPath = Path().apply { addCircle(centerX, centerY, r) }
            canvas.clipPath(clipPath, ClipMode.INTERSECT, true)

            val dst = Rect.makeXYWH(centerX - r, centerY - r, size, size)
            canvas.drawImageRect(
                img,
                Rect.makeWH(img.width.toFloat(), img.height.toFloat()),
                dst,
                SamplingMode.LINEAR,
                Paint().apply { isAntiAlias = true },
                true
            )

            canvas.restoreToCount(saveCount)
            return
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }

        val ch = user.fullName.first().toString()
        val bounds = font.measureText(ch)

        val textX = centerX - (bounds.left + bounds.right) / 2f
        val baselineY = centerY - (bounds.top + bounds.bottom) / 2f

        canvas.drawTextLine(TextLine.make(ch, font), textX, baselineY, textPaint)
    }


    private fun drawDialog(bubbleX: Float, bubbleY: Float, message: Pair<QuoteUser, TdApi.Message>) {
        val sender = message.first.fullName
        val text = (message.second.content as TdApi.MessageText).text.text

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


    fun measureDialogSize(message: Pair<QuoteUser, TdApi.Message>): Pair<Float, Float> {
        val sender = message.first.fullName
        val senderTextSize = font.measureText(sender)
        val textSize = font.measureText((message.second.content as TdApi.MessageText).text.text)

        val bubbleW = maxOf(senderTextSize.width, textSize.width) + padding * (2 + 2) // leftPadding + rightPadding
        val bubbleH =
            senderTextSize.height + textSize.height + padding * (2 + 1 + 2) // topPadding + between + bottomPadding

        return Pair(bubbleW, bubbleH)
    }

    fun measureFullSize(): Pair<Float, Float> {
        var maxW = 0f
        var totalH = 0f
        var first = true

        messages.forEach { entry ->
            val (bubbleW, bubbleH) = measureDialogSize(entry.toPair())
            val rowH = maxOf(avatarSize, bubbleH)
            val rowW = avatarSize + padding + bubbleW

            if (rowW > maxW) maxW = rowW
            totalH += rowH + if (first) 0f else padding
            first = false
        }

        return Pair(maxW, totalH)
    }

    fun render() {
        canvas.clear(Color.TRANSPARENT)

        var endY = 0f

        messages.forEach {
            drawAvatar(endY, avatarSize, it.key)
            drawDialog(avatarSize + padding, endY, it.toPair())

            endY += maxOf(
                measureDialogSize(it.toPair()).second,
                avatarSize
            ) + padding
        }
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
