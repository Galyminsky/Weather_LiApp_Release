package com.protonmail.jobforandroid.weather.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.model.WeatherIconState

class WeatherBackgroundManager(
    private val primaryView: ImageView,
    private val secondaryView: ImageView,
    private val scrimView: View
) {
    private var activeBackgroundIndex = 0
    private var backgroundRequestGeneration = 0
    private var displayedBackgroundResId: Int? = null
    private var pendingBackgroundResId: Int? = null
    private var pendingBackgroundIndex: Int? = null
    private var backgroundAnimator: android.view.ViewPropertyAnimator? = null

    companion object {
        private const val WEATHER_BACKGROUND_CROSSFADE_MS = 450L
    }

    fun applyBackground(state: WeatherIconState?) {
        val resourceId = WeatherBackgroundResolver.resolve(state?.conditionCode)
        if (displayedBackgroundResId == resourceId && pendingBackgroundResId == null) return

        cancelPendingBackgroundTransition()
        val hasDisplayedBackground = displayedBackgroundResId != null
        val targetIndex = if (hasDisplayedBackground) 1 - activeBackgroundIndex else activeBackgroundIndex
        val target = backgroundView(targetIndex)
        val generation = ++backgroundRequestGeneration
        pendingBackgroundResId = resourceId
        pendingBackgroundIndex = targetIndex
        target.alpha = if (hasDisplayedBackground) 0f else 1f
        target.visibility = View.VISIBLE
        scrimView.visibility = View.VISIBLE

        Picasso.get()
            .load(resourceId)
            .fit()
            .centerCrop()
            .noFade()
            .into(target, object : Callback {
                override fun onSuccess() {
                    if (generation != backgroundRequestGeneration) return
                    if (!hasDisplayedBackground) {
                        displayedBackgroundResId = resourceId
                        pendingBackgroundResId = null
                        pendingBackgroundIndex = null
                        return
                    }

                    val current = backgroundView(activeBackgroundIndex)
                    val animator = target.animate()
                        .alpha(1f)
                        .setDuration(WEATHER_BACKGROUND_CROSSFADE_MS)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                if (generation != backgroundRequestGeneration) return
                                current.visibility = View.GONE
                                current.alpha = 1f
                                target.alpha = 1f
                                activeBackgroundIndex = targetIndex
                                displayedBackgroundResId = resourceId
                                pendingBackgroundResId = null
                                pendingBackgroundIndex = null
                                backgroundAnimator = null
                                target.animate().setListener(null)
                            }
                        })
                    backgroundAnimator = animator
                    animator.start()
                }

                override fun onError(error: Exception?) {
                    if (generation != backgroundRequestGeneration) return
                    target.visibility = View.GONE
                    target.alpha = 1f
                    if (!hasDisplayedBackground) scrimView.visibility = View.GONE
                    pendingBackgroundResId = null
                    pendingBackgroundIndex = null
                }
            })
    }

    private fun cancelPendingBackgroundTransition() {
        backgroundRequestGeneration++
        backgroundAnimator?.setListener(null)
        backgroundAnimator?.cancel()
        backgroundAnimator = null
        pendingBackgroundIndex?.let { index ->
            val pendingView = backgroundView(index)
            Picasso.get().cancelRequest(pendingView)
            pendingView.animate().setListener(null).cancel()
            if (index != activeBackgroundIndex || displayedBackgroundResId == null) {
                pendingView.visibility = View.GONE
                pendingView.alpha = 1f
            }
        }
        pendingBackgroundResId = null
        pendingBackgroundIndex = null
    }

    private fun backgroundView(index: Int): ImageView = if (index == 0) primaryView else secondaryView

    fun release() {
        cancelPendingBackgroundTransition()
        Picasso.get().cancelRequest(primaryView)
        Picasso.get().cancelRequest(secondaryView)
        primaryView.setImageDrawable(null)
        secondaryView.setImageDrawable(null)
        primaryView.visibility = View.GONE
        secondaryView.visibility = View.GONE
        displayedBackgroundResId = null
        activeBackgroundIndex = 0
    }
}

object WeatherBackgroundResolver {
    fun resolve(conditionCode: Int?): Int = when (conditionCode) {
        1000 -> R.drawable.weather_clear
        1003 -> R.drawable.weather_partly_cloudy
        1006, 1009 -> R.drawable.weather_overcast

        1012, 1015, 1018, 1021, 1024, 1027, 1030, 1033, 1036, 1039, 1042, 1045, 1048,
        1135, 1147 -> R.drawable.weather_fog

        1066, 1069, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225,
        1237, 1249, 1252, 1255, 1258, 1261, 1264 -> R.drawable.weather_snow

        1087, 1273, 1276, 1279, 1282 -> R.drawable.weather_thunderstorm

        1063, 1072, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195,
        1198, 1201, 1240, 1243, 1246 -> R.drawable.weather_rain

        else -> R.drawable.weather_overcast
    }
}
