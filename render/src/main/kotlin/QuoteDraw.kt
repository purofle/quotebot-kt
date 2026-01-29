package com.github.purofle.quotebot.render

import com.github.purofle.quotebot.tdlibhelper.QuoteUser
import org.drinkless.tdlib.TdApi
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import kotlin.math.max

class QuoteDraw(private val messages: List<Pair<QuoteUser, TdApi.Message>>, fontFile: String) {

    val scale = 2f

    val avatarSize = 50f * scale
    val padding = 8f * scale
    val fontsize = 18f * scale

    private val senderMsgGap = 4f * scale
    private val maxBubbleWidth = 1000f

    val font: Font = Font(FontMgr.default.makeFromFile(fontFile)!!, fontsize)

    private val fontCollection = FontCollection().apply {
        setDefaultFontManager(FontMgr.default)
    }

    lateinit var canvas: Canvas

    init {
        font.edging = FontEdging.ANTI_ALIAS
        font.isSubpixel = true
    }

    private fun fontAscent(): Float = font.metrics.ascent

    private fun fontLineHeightNoLeading(): Float {
        val fm = font.metrics
        return (fm.descent - fm.ascent)
    }

    private fun senderBlockHeight(): Float = fontLineHeightNoLeading()

    private fun drawAvatar(y: Float, size: Float, user: QuoteUser) {
        val centerX = size / 2f
        val centerY = y + size / 2f
        val r = size / 2f

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
        val textLine = TextLine.make(ch, font)
        val bounds = font.measureText(ch)
        val textX = centerX - (bounds.width / 2f) - bounds.left
        val baselineY = centerY - (bounds.top + bounds.bottom) / 2f

        canvas.drawTextLine(textLine, textX, baselineY, textPaint)
    }

    private fun createParagraph(text: String, width: Float): Paragraph {
        val paragraphStyle = ParagraphStyle()
        val textStyle = TextStyle().apply {
            color = Color.WHITE
            fontSize = fontsize
            typeface = font.typeface
        }
        return ParagraphBuilder(paragraphStyle, fontCollection).apply {
            pushStyle(textStyle)
            addText(text)
        }.build().also { it.layout(width) }
    }

    private fun drawDialog(bubbleX: Float, bubbleY: Float, message: Pair<QuoteUser, TdApi.Message>) {
        val sender = message.first.fullName
        val text = (message.second.content as TdApi.MessageText).text.text

        val (bubbleW, bubbleH) = measureDialogSize(message)

        val radius = 12f
        val backgroundPaint = Paint().apply {
            color = Color.makeRGB(26, 20, 41)
            isAntiAlias = true
        }
        canvas.drawRRect(RRect.makeXYWH(bubbleX, bubbleY, bubbleW, bubbleH, radius, radius), backgroundPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }

        val innerX = bubbleX + padding * 2
        val innerTop = bubbleY + padding * 2

        // sender
        val senderBaselineY = innerTop - fontAscent()
        canvas.drawTextLine(TextLine.make(sender, font), innerX, senderBaselineY, textPaint)

        // message
        val messageParagraphWidth = bubbleW - padding * 4
        val messageParagraph = createParagraph(text, messageParagraphWidth)

        val messageTopY = innerTop + senderBlockHeight() + senderMsgGap
        messageParagraph.paint(canvas, innerX, messageTopY)
    }

    fun measureDialogSize(message: Pair<QuoteUser, TdApi.Message>): Pair<Float, Float> {
        val sender = message.first.fullName
        val text = (message.second.content as TdApi.MessageText).text.text

        val maxContentWidth = maxBubbleWidth - padding * 4

        val wrappedParagraph = createParagraph(text, maxContentWidth)
        val intrinsicWidth = minOf(wrappedParagraph.maxIntrinsicWidth, maxContentWidth)

        val senderWidth = font.measureText(sender).width
        val contentWidth = minOf(maxContentWidth, max(senderWidth, intrinsicWidth))

        val textParagraph =
            if (contentWidth == maxContentWidth) wrappedParagraph else createParagraph(text, contentWidth)

        val bubbleW = contentWidth + padding * 4
        val bubbleH =
            (padding * 2) +
                    senderBlockHeight() +
                    senderMsgGap +
                    textParagraph.height +
                    (padding * 2)

        return Pair(bubbleW, bubbleH)
    }

    fun measureFullSize(): Pair<Float, Float> {
        var maxW = 0f
        var totalH = 0f
        var first = true

        messages.forEach { message ->
            val (bubbleW, bubbleH) = measureDialogSize(message)
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

        messages.forEach { message ->
            drawAvatar(endY, avatarSize, message.first)
            drawDialog(avatarSize + padding, endY, message)

            endY += maxOf(
                measureDialogSize(message).second,
                avatarSize
            ) + padding
        }
    }

    fun encodeWebp(quality: Int = 100): Data {
        val size = measureFullSize()
        val info = ImageInfo.makeN32Premul(size.first.toInt(), size.second.toInt())
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
