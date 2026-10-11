package ru.helper.mp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val d = resources.displayMetrics.density
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * d).toInt(), (48 * d).toInt(), (24 * d).toInt(), (24 * d).toInt())
        }
        fun tv(t: String, sp: Float, bold: Boolean = false) = TextView(this).apply {
            text = t; textSize = sp; setTextColor(Color.BLACK)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, (16 * d).toInt())
        }
        col.addView(tv("Помощник по покупкам", 28f, true))
        col.addView(tv("Подсказывает, куда нажимать в Wildberries и Ozon: обводит нужную кнопку и пишет, что делать.", 20f))
        status = tv("", 20f, true); col.addView(status)
        col.addView(Button(this).apply {
            text = "Включить помощника"; textSize = 22f
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        col.addView(tv("\nВ настройках найдите «Помощник по покупкам» и включите переключатель.", 18f))
        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onResume() {
        super.onResume()
        val on = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains(packageName) == true
        status.text = if (on) "✅ Помощник включён. Откройте Wildberries или Ozon." else "⚠️ Помощник пока выключен."
    }
}
