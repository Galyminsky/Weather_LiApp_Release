package com.protonmail.jobforandroid.weather.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.protonmail.jobforandroid.weather.R

/** Soft opposing inner shades for a recessed field. */
class NeomorphicInsetDrawable(context: Context) : Drawable() {
    private val density = context.resources.displayMetrics.density
    private val radius = 16f * density
    private val surfaceColor = context.getColor(R.color.weather_neomorphic_face)
    private val darkInnerColor = context.getColor(R.color.weather_inset_shadow_dark)
    private val lightInnerColor = context.getColor(R.color.weather_inset_shadow_light)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = RectF()
    private val clipPath = Path()

    override fun draw(canvas: Canvas) {
        bounds.set(getBounds())
        if (bounds.isEmpty) return

        clipPath.reset()
        clipPath.addRoundRect(bounds, radius, radius, Path.Direction.CW)
        val save = canvas.save()
        canvas.clipPath(clipPath)

        paint.shader = null
        paint.color = surfaceColor
        canvas.drawRoundRect(bounds, radius, radius, paint)

        val spread = maxOf(bounds.width(), bounds.height()) * 0.9f
        paint.shader = RadialGradient(
            bounds.left, bounds.top, spread,
            darkInnerColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawRect(bounds, paint)

        paint.shader = RadialGradient(
            bounds.right, bounds.bottom, spread,
            lightInnerColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawRect(bounds, paint)
        paint.shader = null
        canvas.restoreToCount(save)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated by Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
