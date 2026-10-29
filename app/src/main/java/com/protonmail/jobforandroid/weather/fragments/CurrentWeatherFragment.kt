package com.protonmail.jobforandroid.weather.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.squareup.picasso.Callback
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.databinding.FragmentCurrentWeatherBinding
import com.protonmail.jobforandroid.weather.model.MainViewModel
import com.protonmail.jobforandroid.weather.model.WeatherIconState
import com.protonmail.jobforandroid.weather.util.WeatherPresentation
import com.protonmail.jobforandroid.weather.widgets.NeomorphicInsetDrawable
import com.squareup.picasso.Picasso
import java.util.Locale

class CurrentWeatherFragment : Fragment() {
    private var _binding: FragmentCurrentWeatherBinding? = null
    private val binding get() = _binding!!
    private val model: MainViewModel by activityViewModels()
    private var currentIllustrationFile: String? = null
    private var latestWeatherImageUrl: String? = null
    private var latestConditionDescription: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCurrentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchCityField.setOnClickListener { (parentFragment as? MainScreenActions)?.searchCity() }
        binding.searchCityInput.setOnClickListener { (parentFragment as? MainScreenActions)?.searchCity() }
        binding.ibSync.setOnClickListener { (parentFragment as? MainScreenActions)?.refreshWeather() }
        binding.searchCityInput.setBoxBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.searchCityInput.background = NeomorphicInsetDrawable(requireContext())
        configureIllustrationWebView(binding.weatherIllustration)
        model.liveDataToday.observe(viewLifecycleOwner) { weather ->
            binding.tvData.text = formatUpdateTime(weather.time)
            binding.tvCity.text = weather.city
            binding.tvCurrentTemp.text = WeatherPresentation.roundedTemperature(weather.currentTemp)
                ?.let { "$it°C" } ?: getString(R.string.weather_metric_missing)
            val localizedCondition = WeatherPresentation.condition(weather.condition)
            binding.tvCondition.text = localizedCondition
            binding.tvSunrise.text = WeatherPresentation.localizedClockTime(weather.sunrise)
            binding.tvSunset.text = WeatherPresentation.localizedClockTime(weather.sunset)
            latestConditionDescription = localizedCondition
            binding.tvFeelsLike.text = getString(R.string.weather_feels_like_missing)
            val highLow = when {
                WeatherPresentation.roundedTemperature(weather.maxTemp) != null &&
                    WeatherPresentation.roundedTemperature(weather.minTemp) != null ->
                    getString(R.string.weather_high_low,
                        WeatherPresentation.roundedTemperature(weather.maxTemp),
                        WeatherPresentation.roundedTemperature(weather.minTemp))
                WeatherPresentation.roundedTemperature(weather.maxTemp) != null ->
                    getString(R.string.weather_high_only, WeatherPresentation.roundedTemperature(weather.maxTemp))
                WeatherPresentation.roundedTemperature(weather.minTemp) != null ->
                    getString(R.string.weather_low_only, WeatherPresentation.roundedTemperature(weather.minTemp))
                else -> getString(R.string.weather_metric_missing)
            }
            binding.tvMaxMin.text = highLow
            latestWeatherImageUrl = weather.imageUrl.takeIf { it.isNotBlank() }
                ?.let { "https:$it" }
            renderWeatherIllustration(model.liveDataWeatherIcon.value)
        }
        applyMissingMetrics()
        model.liveDataTodayMetrics.observe(viewLifecycleOwner) { metrics ->
            binding.tvFeelsLike.text = metrics.feelsLikeC?.let {
                WeatherPresentation.roundedTemperature(it)?.let { rounded ->
                    getString(R.string.weather_feels_like, rounded)
                } ?: getString(R.string.weather_feels_like_missing)
            } ?: getString(R.string.weather_feels_like_missing)
            binding.tvWindValue.text = metrics.windSpeedKph?.let {
                getString(R.string.weather_wind_speed, it)
            } ?: getString(R.string.weather_metric_missing)
            binding.tvWindDirection.text = metrics.windDirection?.let(::localizedWindDirection)
                ?: getString(R.string.weather_metric_missing)
            binding.tvHumidityValue.text = metrics.humidityPercent?.let {
                getString(R.string.weather_percent_value, it)
            } ?: getString(R.string.weather_metric_missing)
            binding.tvPrecipitationValue.text = metrics.precipitationChancePercent?.let {
                getString(R.string.weather_percent_value, it)
            } ?: getString(R.string.weather_metric_missing)
            binding.tvPressureValue.text = metrics.pressureHpa?.let {
                getString(R.string.weather_pressure_value, it)
            } ?: getString(R.string.weather_metric_missing)
        }
        model.liveDataWeatherIcon.observe(viewLifecycleOwner) { state ->
            renderWeatherIllustration(state)
        }
        applyDefaultBackground()
        model.liveDataWeatherBackdrop.observe(viewLifecycleOwner) { state ->
            binding.root.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(state.topColor, state.bottomColor)
            )
        }
    }
    private fun applyDefaultBackground() {
        val color = requireContext().getColor(R.color.weather_background)
        binding.root.setBackgroundColor(color)
    }

    private fun applyMissingMetrics() {
        binding.tvFeelsLike.text = getString(R.string.weather_feels_like_missing)
        binding.tvWindValue.text = getString(R.string.weather_metric_missing)
        binding.tvWindDirection.text = getString(R.string.weather_metric_missing)
        binding.tvHumidityValue.text = getString(R.string.weather_metric_missing)
        binding.tvPrecipitationValue.text = getString(R.string.weather_metric_missing)
        binding.tvPressureValue.text = getString(R.string.weather_metric_missing)
    }

    private fun formatUpdateTime(value: String): String {
        val time = value.substringAfter(' ', value).trim().ifBlank { value }
        return getString(R.string.weather_updated_at, time)
    }

    private fun configureIllustrationWebView(webView: WebView) {
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        webView.isFocusable = false
        webView.isClickable = false
        webView.settings.apply {
            javaScriptEnabled = false
            domStorageEnabled = false
            allowFileAccess = true
            allowContentAccess = false
            blockNetworkLoads = true
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                val path = request?.url?.lastPathSegment.orEmpty()
                if (request?.isForMainFrame == true || path.endsWith(".svg")) {
                    showApiWeatherFallback()
                }
            }
        }
    }

    private fun renderWeatherIllustration(state: WeatherIconState?) {
        if (_binding == null) return
        val assetFile = resolveWeatherIllustration(state)
        if (assetFile == null) {
            showApiWeatherFallback()
            return
        }

        currentIllustrationFile = assetFile
        Picasso.get().cancelRequest(binding.imWeather)
        binding.weatherIllustration.visibility = View.VISIBLE
        binding.imWeather.visibility = View.GONE
        val html = """
            <!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
            <style>html,body{width:100%;height:100%;margin:0;padding:0;background:rgba(0,0,0,0)!important;overflow:hidden}
            img{display:block;width:100%;height:100%;background:transparent;object-fit:contain}</style></head>
            <body><img alt="" src="$assetFile"></body></html>
        """.trimIndent()
        binding.weatherIllustration.loadDataWithBaseURL(
            "file:///android_asset/weather_icons/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun showApiWeatherFallback() {
        if (_binding == null) return
        currentIllustrationFile = null
        binding.weatherIllustration.stopLoading()
        binding.weatherIllustration.visibility = View.GONE
        binding.imWeather.contentDescription = latestConditionDescription.ifBlank {
            getString(R.string.weather_image)
        }
        binding.imWeather.visibility = View.VISIBLE
        binding.imWeather.setImageResource(R.drawable.ic_weather_unknown)
        Picasso.get().cancelRequest(binding.imWeather)
        val imageUrl = latestWeatherImageUrl ?: return
        Picasso.get().load(imageUrl).into(binding.imWeather, object : Callback {
            override fun onSuccess() = Unit
            override fun onError(error: Exception?) {
                if (_binding != null && currentIllustrationFile == null) {
                    binding.imWeather.setImageResource(R.drawable.ic_weather_unknown)
                }
            }
        })
    }

    private fun localizedWindDirection(direction: String): String = when (direction.uppercase(Locale.ROOT)) {
        "N" -> getString(R.string.wind_north)
        "NNE" -> getString(R.string.wind_north_northeast)
        "NE" -> getString(R.string.wind_northeast)
        "ENE" -> getString(R.string.wind_east_northeast)
        "E" -> getString(R.string.wind_east)
        "ESE" -> getString(R.string.wind_east_southeast)
        "SE" -> getString(R.string.wind_southeast)
        "SSE" -> getString(R.string.wind_south_southeast)
        "S" -> getString(R.string.wind_south)
        "SSW" -> getString(R.string.wind_south_southwest)
        "SW" -> getString(R.string.wind_southwest)
        "WSW" -> getString(R.string.wind_west_southwest)
        "W" -> getString(R.string.wind_west)
        "WNW" -> getString(R.string.wind_west_northwest)
        "NW" -> getString(R.string.wind_northwest)
        "NNW" -> getString(R.string.wind_north_northwest)
        else -> direction
    }

    private fun resolveWeatherIllustration(state: WeatherIconState?): String? {
        return WeatherIconResolver.resolve(state)
    }

    override fun onDestroyView() {
        _binding?.weatherIllustration?.apply {
            stopLoading()
            loadUrl("about:blank")
            destroy()
        }
        currentIllustrationFile = null
        latestWeatherImageUrl = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic fun newInstance() = CurrentWeatherFragment()
    }
}

internal object WeatherIconResolver {
    fun resolve(state: WeatherIconState?): String? {
        state ?: return null
        val isDay = state.isDay
        return when (state.conditionCode) {
            1000 -> when (isDay) {
                true -> "day.svg"
                false -> "night.svg"
                null -> null
            }
            1003 -> when (isDay) {
                true -> "cloudy-day-1.svg"
                false -> "cloudy-night-1.svg"
                null -> "cloudy.svg"
            }
            1006, 1009 -> "cloudy.svg"

            1063 -> if (isDay == true) "rainy-2.svg" else "rainy-4.svg"
            1150, 1153, 1180, 1183 -> if (isDay == true) "rainy-2.svg" else "rainy-4.svg"
            1186, 1189 -> "rainy-5.svg"
            1192, 1195, 1243, 1246 -> "rainy-6.svg"
            1240 -> if (isDay == true) "rainy-2.svg" else "rainy-4.svg"

            1066, 1210, 1255 -> if (isDay == true) "snowy-2.svg" else "snowy-5.svg"
            1114, 1117, 1222, 1225, 1258 -> "snowy-6.svg"
            1213, 1216, 1219 -> "snowy-5.svg"

            1087, 1273, 1276 -> "thunder.svg"
            else -> null
        }
    }
}
