package com.protonmail.jobforandroid.weather.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import com.protonmail.jobforandroid.weather.R

/** Material card with a soft light/dark relief rendered inside its bounds. */
class NeomorphicCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var inset = dp(16f)
    private var blur = dp(10f)
    private var offset = dp(4f)
    private var neonGlowColor = Color.TRANSPARENT
    private var neonGlowRadius = 0f
    private var faceOpacity = 0.25f
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val faceBounds = RectF()
    private val neonGlowBounds = RectF()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.NeomorphicCardView, defStyleAttr, 0).apply {
            inset = getDimension(R.styleable.NeomorphicCardView_neomorphicInset, inset)
            blur = getDimension(R.styleable.NeomorphicCardView_neomorphicBlur, blur)
            offset = getDimension(R.styleable.NeomorphicCardView_neomorphicOffset, offset)
            neonGlowColor = getColor(R.styleable.NeomorphicCardView_neomorphicGlowColor, neonGlowColor)
            neonGlowRadius = getDimension(R.styleable.NeomorphicCardView_neomorphicGlowRadius, neonGlowRadius)
            faceOpacity = getFloat(R.styleable.NeomorphicCardView_neomorphicFaceOpacity, faceOpacity)
                .coerceIn(0f, 1f)
            recycle()
        }
        // Paint shadow layers need the software canvas and stay inside the reserved view rim.
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setCardBackgroundColor(Color.TRANSPARENT)
        cardElevation = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= inset * 2f || height <= inset * 2f) return

        drawNeonGlow(canvas)

        faceBounds.set(inset, inset, width - inset, height - inset)
        val faceColor = Color.WHITE
        val cornerRadius = (radius - inset * 0.35f).coerceAtLeast(inset * 0.6f)

        shadowPaint.color = withAlpha(faceColor, faceOpacity)
        shadowPaint.setShadowLayer(blur, -offset, -offset, context.getColor(R.color.weather_shadow_light))
        canvas.drawRoundRect(faceBounds, cornerRadius, cornerRadius, shadowPaint)
        shadowPaint.clearShadowLayer()

        shadowPaint.setShadowLayer(blur, offset, offset, context.getColor(R.color.weather_shadow_dark))
        canvas.drawRoundRect(faceBounds, cornerRadius, cornerRadius, shadowPaint)
        shadowPaint.clearShadowLayer()
    }

    fun setNeonGlow(color: Int, radiusDp: Float) {
        val radius = dp(radiusDp)
        if (neonGlowColor == color && neonGlowRadius == radius) return
        neonGlowColor = color
        neonGlowRadius = radius
        invalidate()
    }

    private fun drawNeonGlow(canvas: Canvas) {
        if (neonGlowColor == Color.TRANSPARENT || neonGlowRadius <= 0f) return

        val edgeInset = dp(2f)
        neonGlowBounds.set(edgeInset, edgeInset, width - edgeInset, height - edgeInset)
        neonGlowPaint.strokeWidth = dp(1.5f)
        neonGlowPaint.color = withAlpha(neonGlowColor, 0.24f)
        neonGlowPaint.setShadowLayer(neonGlowRadius, 0f, 0f, withAlpha(neonGlowColor, 0.68f))
        val glowCornerRadius = (radius - edgeInset).coerceAtLeast(0f)
        canvas.drawRoundRect(neonGlowBounds, glowCornerRadius, glowCornerRadius, neonGlowPaint)
        neonGlowPaint.clearShadowLayer()
    }

    private fun withAlpha(color: Int, fraction: Float): Int = Color.argb(
        (Color.alpha(color) * fraction).toInt(),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
