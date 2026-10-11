package ru.helper.mp

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** Один шаг покупки: если на экране есть одна из надписей — обводим её и показываем подсказку. */
data class Step(val texts: List<String>, val hint: String)

class HelperService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = Runnable { update() }
    private var overlay: HintView? = null

    // Порядок важен: сверху — самые поздние шаги покупки. Тексты кнопок при желании правьте здесь.
    private fun steps(pkg: String): List<Step> {
        val search = if (pkg == OZON) "Искать на Ozon" else "Искать на Wildberries"
        return listOf(
            Step(listOf("Оплатить", "Подтвердить заказ"), "Последний шаг! Нажмите сюда, чтобы оплатить заказ"),
            Step(listOf("Оформить заказ", "Перейти к оформлению"), "Нажмите сюда, чтобы оформить заказ"),
            Step(listOf("Перейти в корзину"), "Товар в корзине. Нажмите сюда, чтобы открыть корзину"),
            Step(listOf("Добавить в корзину", "В корзину"), "Нажмите сюда, чтобы положить товар в корзину"),
            Step(listOf(search), "Нажмите сюда и напишите, что хотите найти"),
        )
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        handler.removeCallbacks(refresh); handler.postDelayed(refresh, 350)
    }
    override fun onInterrupt() = hide()
    override fun onDestroy() { hide(); super.onDestroy() }

    private fun update() {
        val root = rootInActiveWindow ?: return hide()
        val pkg = root.packageName?.toString() ?: return hide()
        if (pkg != WB && pkg != OZON) return hide()
        for (s in steps(pkg)) {
            val rect = findTarget(root, s.texts) ?: continue
            show(rect, s.hint); return
        }
        hide()
    }

    private fun findTarget(root: AccessibilityNodeInfo, texts: List<String>): Rect? {
        val screenH = resources.displayMetrics.heightPixels
        for (t in texts) for (n in root.findAccessibilityNodeInfosByText(t)) {
            if (!n.isVisibleToUser) continue
            val node = clickableAncestor(n)
            val r = Rect(); node.getBoundsInScreen(r)
            if (r.width() > 0 && r.height() > 0 && r.height() < screenH * 0.5) return r
        }
        return null
    }

    private fun clickableAncestor(n: AccessibilityNodeInfo): AccessibilityNodeInfo {
        var c = n
        repeat(6) { if (c.isClickable) return c; c = c.parent ?: return n }
        return n
    }

    private fun show(r: Rect, hint: String) {
        if (overlay == null) {
            val v = HintView(this)
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(v, lp)
            overlay = v
        }
        overlay?.show(r, hint)
    }

    private fun hide() {
        overlay?.let { it.stop(); runCatching { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) } }
        overlay = null
    }

    companion object { const val WB = "com.wildberries.ru"; const val OZON = "ru.ozon.app.android" }
}
