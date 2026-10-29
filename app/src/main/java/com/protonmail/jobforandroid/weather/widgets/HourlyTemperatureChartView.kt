package com.protonmail.jobforandroid.weather.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class ChartPoint(val timeLabel: String, val temp: Float)

class HourlyTemperatureChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var points: List<ChartPoint> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = 0xFF2196F3.toInt()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x222196F3.toInt()
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF2196F3.toInt()
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = 0xFF757575.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val tempTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 26f
        color = 0xFF212121.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val fillPath = Path()
    private val linePath = Path()

    fun setHoursJson(hoursJson: String?) {
        if (hoursJson.isNullOrBlank()) {
            points = emptyList()
            invalidate()
            return
        }
        val list = mutableListOf<ChartPoint>()
        runCatching {
            val array = org.json.JSONArray(hoursJson)
            val targetHours = listOf(0, 3, 6, 9, 12, 15, 18, 21)
            for (h in targetHours) {
                if (h < array.length()) {
                    val obj = array.optJSONObject(h)
                    if (obj != null) {
                        val temp = obj.optString("temp_c").toFloatOrNull()
                        val timeStr = obj.optString("time")
                        val timeLabel = if (timeStr.length >= 16) timeStr.takeLast(5) else String.format(Locale.getDefault(), "%02d:00", h)
                        if (temp != null) {
                            list.add(ChartPoint(timeLabel, temp))
                        }
                    }
                }
            }
        }
        points = list
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (100 * resources.displayMetrics.density).toInt()
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val padTop = 32f
        val padBottom = 32f
        val padLeft = 32f
        val padRight = 32f

        val w = width.toFloat() - padLeft - padRight
        val h = height.toFloat() - padTop - padBottom

        val minTemp = points.minOf { it.temp }
        val maxTemp = points.maxOf { it.temp }
        val tempRange = max(1f, maxTemp - minTemp)

        val stepX = if (points.size > 1) w / (points.size - 1) else w

        val xs = FloatArray(points.size)
        val ys = FloatArray(points.size)

        points.forEachIndexed { index, pt ->
            xs[index] = padLeft + index * stepX
            ys[index] = padTop + h - ((pt.temp - minTemp) / tempRange) * h
        }

        // Fill path
        fillPath.reset()
        fillPath.moveTo(xs[0], padTop + h)
        for (i in xs.indices) {
            fillPath.lineTo(xs[i], ys[i])
        }
        fillPath.lineTo(xs.last(), padTop + h)
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)

        // Line path
        linePath.reset()
        linePath.moveTo(xs[0], ys[0])
        for (i in 0 until xs.size - 1) {
            val midX = (xs[i] + xs[i + 1]) / 2f
            linePath.quadTo(xs[i], ys[i], midX, (ys[i] + ys[i + 1]) / 2f)
        }
        linePath.lineTo(xs.last(), ys.last())
        canvas.drawPath(linePath, linePaint)

        // Dots, temperatures, and time labels
        points.forEachIndexed { index, pt ->
            val cx = xs[index]
            val cy = ys[index]
            canvas.drawCircle(cx, cy, 5f, dotPaint)
            val tempInt = pt.temp.roundToInt()
            canvas.drawText("$tempInt°", cx, cy - 10f, tempTextPaint)
            canvas.drawText(pt.timeLabel, cx, padTop + h + 24f, textPaint)
        }
    }
}
