package ru.helper.mp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View

class HintView(ctx: Context) : View(ctx) {
    private val d = ctx.resources.displayMetrics.density
    private var target: Rect? = null
    private var text = ""
    private var pulse = 0f
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFFFF3D00.toInt(); strokeWidth = 5 * d }
    private val bubble = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF212121.toInt() }
    private val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 20 * d; isFakeBoldText = true }
    private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 700; repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE
        addUpdateListener { pulse = it.animatedValue as Float; invalidate() }
    }

    fun show(r: Rect, t: String) { target = r; text = t; if (!anim.isRunning) anim.start(); invalidate() }
    fun stop() { anim.cancel() }

    override fun onDraw(c: Canvas) {
        val r = target ?: return
        val loc = IntArray(2); getLocationOnScreen(loc)
        val box = RectF(r).apply { offset(-loc[0].toFloat(), -loc[1].toFloat()); val g = (4 + 8 * pulse) * d; inset(-g, -g) }
        c.drawRoundRect(box, 14 * d, 14 * d, ring)

        val pad = 12 * d
        val bw = width - 32 * d
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, (bw - 2 * pad).toInt()).build()
        val bh = layout.height + 2 * pad
        var top = box.top - bh - 14 * d
        if (top < 24 * d) top = box.bottom + 14 * d
        if (top + bh > height - 8 * d) top = height - bh - 8 * d
        c.drawRoundRect(16 * d, top, 16 * d + bw, top + bh, 14 * d, 14 * d, bubble)
        c.save(); c.translate(16 * d + pad, top + pad); layout.draw(c); c.restore()
    }
}
