package com.protonmail.jobforandroid.weather.widgets

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView

/** Card surface with a flat selection outline. */
class NeomorphicCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    fun setNeonGlow(color: Int, radiusDp: Float) {
        strokeWidth = if (radiusDp > 0f) (1.5f * resources.displayMetrics.density).toInt() else 0
        strokeColor = color
    }
}
