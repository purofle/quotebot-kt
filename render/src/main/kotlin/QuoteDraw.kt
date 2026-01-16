package com.github.purofle.quotebot.render

import org.jetbrains.skia.*

class QuoteDraw(private val text: String) {

    fun drawAvatar(canvas: Canvas, scale: Float, x: Float, y: Float, size: Float) {
        // TODO: draw avatar
    }

    fun render(canvas: Canvas, width: Int = 512, height: Int = 512) {
        canvas.clear(Color.TRANSPARENT)

        val scale = 1f

        val padding = 20f * scale
        val radius = 20f * scale

        // 左侧预留头像位置
        val avatarSize = 50f * scale
        val gap = 10f * scale
        val bubbleX = padding + avatarSize + gap
        val bubbleY = padding
        val bubbleW = width.toFloat() - bubbleX - padding
        val bubbleH = height.toFloat() - padding * 2

        // 画圆角气泡
        val backgroundPaint = Paint().apply {
            color = Color.makeRGB(26, 20, 41)
            isAntiAlias = true
        }
        val bubble = RRect.makeXYWH(bubbleX, bubbleY, bubbleW, bubbleH, radius, radius)
        canvas.drawRRect(bubble, backgroundPaint)

        // 画头像占位
        drawAvatar(canvas, scale, padding, bubbleY + 5f * scale, avatarSize)

        // 气泡左边开始 + 垂直居中
        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        val font = Font(null, 24f * scale)

        val line = TextLine.make(text, font)

        val textX = bubbleX + 10f * scale

        // 垂直居中
        val fm = font.metrics
        val textAreaTop = bubbleY
        val textAreaBottom = bubbleY + bubbleH
        val baselineY = (textAreaTop + textAreaBottom) / 2f - (fm.ascent + fm.descent) / 2f

        canvas.drawTextLine(line, textX, baselineY, textPaint)
    }

    fun encodeWebp(width: Int, height: Int, quality: Int = 90): Data {
        val info = ImageInfo.makeN32Premul(width, height)
        Surface.makeRaster(info).use { surface ->
            render(surface.canvas, width, height)
            surface.flushAndSubmit()
            surface.makeImageSnapshot().use { img ->
                return img.encodeToData(EncodedImageFormat.WEBP, quality)
                    ?: error("WEBP encoder returned null (codec missing?)")
            }
        }
    }
}
