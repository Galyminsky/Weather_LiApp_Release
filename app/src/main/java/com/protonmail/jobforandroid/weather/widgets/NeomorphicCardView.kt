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
    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val faceBounds = RectF()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.NeomorphicCardView).apply {
            inset = getDimension(R.styleable.NeomorphicCardView_neomorphicInset, inset)
            blur = getDimension(R.styleable.NeomorphicCardView_neomorphicBlur, blur)
            offset = getDimension(R.styleable.NeomorphicCardView_neomorphicOffset, offset)
            recycle()
        }
        // Paint shadow layers need the software canvas and stay inside the reserved view rim.
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setCardBackgroundColor(Color.TRANSPARENT)
        cardElevation = 0f
        setStrokeWidth(0)
        setStrokeColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= inset * 2f || height <= inset * 2f) return

        faceBounds.set(inset, inset, width - inset, height - inset)
        val faceColor = context.getColor(R.color.weather_neomorphic_face)
        val cornerRadius = (radius - inset * 0.35f).coerceAtLeast(inset * 0.6f)

        shadowPaint.color = faceColor
        shadowPaint.setShadowLayer(blur, -offset, -offset, context.getColor(R.color.weather_shadow_light))
        canvas.drawRoundRect(faceBounds, cornerRadius, cornerRadius, shadowPaint)
        shadowPaint.clearShadowLayer()

        shadowPaint.setShadowLayer(blur, offset, offset, context.getColor(R.color.weather_shadow_dark))
        canvas.drawRoundRect(faceBounds, cornerRadius, cornerRadius, shadowPaint)
        shadowPaint.clearShadowLayer()

        facePaint.color = faceColor
        canvas.drawRoundRect(faceBounds, cornerRadius, cornerRadius, facePaint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
