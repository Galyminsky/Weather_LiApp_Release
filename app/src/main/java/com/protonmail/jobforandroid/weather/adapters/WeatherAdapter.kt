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
            tvDate.text = WeatherPresentation.forecastDate(item.time, hourly = item.currentTemp.isNotBlank())
            tvCondition.text = WeatherPresentation.condition(item.condition)
            val missing = itemView.context.getString(R.string.weather_metric_missing)
            val current = WeatherPresentation.roundedTemperature(item.currentTemp)
            val high = WeatherPresentation.roundedTemperature(item.maxTemp)
            val low = WeatherPresentation.roundedTemperature(item.minTemp)
            tvTemp.text = if (item.currentTemp.isBlank()) {
                if (high != null && low != null) "$high°C / $low°C"
                else high?.let { "$it°C" } ?: low?.let { "$it°C" } ?: missing
            } else {
                current?.let { "$it°C" } ?: missing
            }
            Picasso.get().load("https:${item.imageUrl}").into(im)
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
