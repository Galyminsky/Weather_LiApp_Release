package com.protonmail.jobforandroid.weather.adapters

import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.databinding.ItemHourForecastBinding
import com.protonmail.jobforandroid.weather.fragments.HourForecast
import com.squareup.picasso.Picasso

class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.HourHolder>() {
    private var items: List<HourForecast> = emptyList()
    private var expandedPosition = 0
    private var attachedRecyclerView: RecyclerView? = null

    init { setHasStableIds(true) }

    fun submitList(value: List<HourForecast>) {
        val expandedEpoch = items.getOrNull(expandedPosition)?.epoch
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = value.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition].epoch == value[newItemPosition].epoch
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition] == value[newItemPosition]
        })
        items = value
        expandedPosition = value.indexOfFirst { it.epoch == expandedEpoch }.takeIf { it >= 0 }
            ?: if (value.isEmpty()) RecyclerView.NO_POSITION else 0
        diff.dispatchUpdatesTo(this)
        if (expandedPosition != RecyclerView.NO_POSITION) notifyItemChanged(expandedPosition)
    }

    override fun getItemId(position: Int) = items[position].epoch

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (attachedRecyclerView === recyclerView) attachedRecyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourHolder =
        HourHolder(ItemHourForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: HourHolder, position: Int) = holder.bind(items[position], position == expandedPosition)

    inner class HourHolder(private val binding: ItemHourForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val previous = expandedPosition
                expandedPosition = if (previous == position) RecyclerView.NO_POSITION else position
                val transition = TransitionSet()
                    .addTransition(ChangeBounds())
                    .addTransition(Fade().addTarget(R.id.expandedDetails))
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .setDuration(280)
                    .setInterpolator(android.view.animation.PathInterpolator(0.2f, 0f, 0f, 1f))
                attachedRecyclerView?.let {
                    TransitionManager.endTransitions(it)
                    TransitionManager.beginDelayedTransition(it, transition)
                }
                if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
                if (expandedPosition != RecyclerView.NO_POSITION) notifyItemChanged(expandedPosition)
            }
        }

        fun bind(hour: HourForecast, expanded: Boolean) = with(binding) {
            val context = root.context
            val missing = context.getString(R.string.weather_metric_missing)
            tvTime.text = hour.time.takeLast(5).ifBlank { missing }
            val isToday = hour.time.substringBefore(' ') == items.firstOrNull()?.time?.substringBefore(' ')
            tvDayLabel.text = context.getString(if (isToday) R.string.weather_today else R.string.weather_tomorrow)
            tvConditionCompact.text = hour.condition.ifBlank { missing }
            tvRainLabelCompact.text = context.getString(R.string.weather_precipitation)
            tvCompactTemperature.text = hour.temperature?.let { "${it.toInt()}°" } ?: missing
            tvRain.text = hour.rain?.let { "$it%" } ?: missing
            tvExpandedPressure.text = hour.pressure ?: missing
            tvExpandedWindSpeed.text = hour.windSpeedMps ?: missing
            tvExpandedHumidity.text = hour.humidity?.let { "$it%" } ?: missing
            tvExpandedFeelsLike.text = hour.feelsLike ?: missing
            val iconUrl = hour.icon.takeIf { it.isNotBlank() }?.let { if (it.startsWith("http")) it else "https:$it" }
            Picasso.get().load(iconUrl).placeholder(R.drawable.ic_weather_unknown).error(R.drawable.ic_weather_unknown).into(ivCompactIcon)
            expandedDetails.visibility = if (expanded) View.VISIBLE else View.GONE
            root.isSelected = expanded
            root.contentDescription = "${hour.time}, ${hour.temperature?.let { "${it.toInt()}°" } ?: missing}, ${if (expanded) "открыто" else "свернуто"}"
        }
    }
}
