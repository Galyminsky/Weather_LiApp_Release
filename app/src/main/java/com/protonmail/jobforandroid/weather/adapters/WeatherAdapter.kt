package com.protonmail.jobforandroid.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.databinding.ListItemBinding
import com.protonmail.jobforandroid.weather.model.WeatherModel
import com.protonmail.jobforandroid.weather.util.WeatherPresentation
import com.squareup.picasso.Picasso
import java.util.Locale
import kotlin.math.roundToInt

class WeatherAdapter(private val listener: Listener?) :
    ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {

    class Holder(view: View, private val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        var itemTemp: WeatherModel? = null

        init {
            itemView.setOnClickListener {
                itemTemp?.let { item -> listener?.onClick(item) }
            }
        }

        fun bind(item: WeatherModel) = with(binding) {
            itemTemp = item
            val context = itemView.context
            tvDate.text = WeatherPresentation.forecastDate(item.time, hourly = item.currentTemp.isNotBlank())
            tvCondition.text = WeatherPresentation.condition(item.condition)
            val missing = context.getString(R.string.weather_metric_missing)
            val high = WeatherPresentation.roundedTemperature(item.maxTemp)
            val low = WeatherPresentation.roundedTemperature(item.minTemp)
            tvTemp.text = if (high != null && low != null) "$high° / $low°"
            else high?.let { "$it°" } ?: low?.let { "$it°" } ?: missing

            Picasso.get().load(if (item.imageUrl.startsWith("http")) item.imageUrl else "https:${item.imageUrl}").placeholder(R.drawable.ic_weather_unknown).error(R.drawable.ic_weather_unknown).into(im)
            chartView.setHoursJson(item.hours)

            var sumHumidity = 0.0
            var countHumidity = 0
            var maxWindKph = 0.0
            var sumPressure = 0.0
            var countPressure = 0
            var maxRainChance = 0

            runCatching {
                val array = org.json.JSONArray(item.hours)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val hum = obj.optString("humidity").toDoubleOrNull()
                    if (hum != null) {
                        sumHumidity += hum
                        countHumidity++
                    }
                    val wind = obj.optString("wind_kph").toDoubleOrNull()
                    if (wind != null && wind > maxWindKph) {
                        maxWindKph = wind
                    }
                    val pres = obj.optString("pressure_mb").toDoubleOrNull()
                    if (pres != null) {
                        sumPressure += pres
                        countPressure++
                    }
                    val rain = obj.optString("chance_of_rain").toIntOrNull() ?: 0
                    val snow = obj.optString("chance_of_snow").toIntOrNull() ?: 0
                    val rainChance = maxOf(rain, snow)
                    if (rainChance > maxRainChance) {
                        maxRainChance = rainChance
                    }
                }
            }

            val avgHumidity = if (countHumidity > 0) (sumHumidity / countHumidity).roundToInt() else null
            val windMps = if (maxWindKph > 0.0) {
                val mps = maxWindKph / 3.6
                if (mps % 1.0 == 0.0) "${mps.toInt()} м/с" else String.format(Locale.getDefault(), "%.1f м/с", mps)
            } else null
            val avgPressure = if (countPressure > 0) {
                ((sumPressure / countPressure) * 0.750062).roundToInt()
            } else null

            tvDailyPressure.text = avgPressure?.let { "$it мм рт. ст." } ?: missing
            tvDailyWindSpeed.text = windMps ?: missing
            tvDailyHumidity.text = avgHumidity?.let { context.getString(R.string.weather_percent_value, it.toString()) } ?: missing
            tvDailyPrecipitation.text = context.getString(R.string.weather_percent_value, maxRainChance.toString())
        }
    }

    class Comparator : DiffUtil.ItemCallback<WeatherModel>() {
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    interface Listener {
        fun onClick(item: WeatherModel)
    }
}
