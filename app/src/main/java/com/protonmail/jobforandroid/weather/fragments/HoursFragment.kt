package com.protonmail.jobforandroid.weather.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.adapters.HourlyForecastAdapter
import com.protonmail.jobforandroid.weather.databinding.FragmentHoursBinding
import com.protonmail.jobforandroid.weather.model.MainViewModel
import java.util.Locale
import kotlin.math.roundToInt

class HoursFragment : Fragment() {
    private var _binding: FragmentHoursBinding? = null
    private val binding get() = _binding!!
    private val model: MainViewModel by activityViewModels()
    private lateinit var adapter: HourlyForecastAdapter
    private var hours: List<HourForecast> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = HourlyForecastAdapter()
        binding.rcView.layoutManager = LinearLayoutManager(requireContext())
        binding.rcView.itemAnimator = null
        binding.rcView.adapter = adapter
        model.liveDataList.observe(viewLifecycleOwner) { days ->
            hours = parseHours(days.orEmpty())
            render()
        }
    }

    private fun parseHours(days: List<com.protonmail.jobforandroid.weather.model.WeatherModel>): List<HourForecast> {
        val all = days.flatMap { day ->
            runCatching {
                val array = org.json.JSONArray(day.hours)
                (0 until array.length()).mapNotNull { index ->
                    val o = array.optJSONObject(index) ?: return@mapNotNull null
                    val condition = o.optJSONObject("condition")
                    HourForecast(
                        epoch = o.optLong("time_epoch", 0L), time = o.optString("time"),
                        temperature = o.optString("temp_c").toDoubleOrNull(),
                        wind = o.optString("wind_kph").metric(), direction = o.optString("wind_dir").takeUnless { it == "null" || it.isBlank() },
                        humidity = o.optString("humidity").metric(), rain = maxOfNullable(o.optString("chance_of_rain").metric(), o.optString("chance_of_snow").metric()),
                        icon = condition?.optString("icon").orEmpty(),
                        condition = condition?.optString("text").orEmpty(),
                        pressure = o.optString("pressure_mb").toDoubleOrNull()?.let { "${(it * 0.750062).roundToInt()} мм рт. ст." },
                        feelsLike = o.optString("feelslike_c").toDoubleOrNull()?.let { "${it.toInt()}°C" },
                        windSpeedMps = o.optString("wind_kph").toDoubleOrNull()?.let {
                            val mps = it / 3.6
                            if (mps % 1.0 == 0.0) "${mps.toInt()} м/с" else "${String.format(Locale.getDefault(), "%.1f", mps)} м/с"
                        }
                    ).takeIf { it.epoch > 0 && it.temperature != null }
                }
            }.getOrDefault(emptyList())
        }.sortedBy { it.epoch }
        val now = System.currentTimeMillis() / 1000L
        val start = all.indexOfFirst { it.epoch >= now }.let { if (it < 0) 0 else it }
        return all.drop(start).take(24)
    }

    private fun render() {
        val hasData = hours.isNotEmpty()
        binding.tvEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.rcView.visibility = if (hasData) View.VISIBLE else View.GONE
        if (!hasData) return
        adapter.submitList(hours)
    }

    override fun onDestroyView() {
        _binding?.rcView?.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object { @JvmStatic fun newInstance() = HoursFragment() }
}

data class HourForecast(
    val epoch: Long,
    val time: String,
    val temperature: Double?,
    val wind: String?,
    val direction: String?,
    val humidity: String?,
    val rain: String?,
    val icon: String,
    val condition: String,
    val pressure: String? = null,
    val feelsLike: String? = null,
    val windSpeedMps: String? = null
)

private fun String.metric(): String? = toDoubleOrNull()?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
private fun maxOfNullable(a: String?, b: String?): String? = listOfNotNull(a?.toIntOrNull(), b?.toIntOrNull()).maxOrNull()?.toString()
