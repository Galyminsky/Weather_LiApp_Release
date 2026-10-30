package com.protonmail.jobforandroid.weather.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.adapters.VpAdapter
import com.protonmail.jobforandroid.weather.databinding.FragmentForecastBinding
import com.protonmail.jobforandroid.weather.model.MainViewModel
import com.protonmail.jobforandroid.weather.util.WeatherBackgroundManager

class ForecastFragment : Fragment() {
    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!
    private var tabLayoutMediator: TabLayoutMediator? = null
    private val model: MainViewModel by activityViewModels()
    private lateinit var backgroundManager: WeatherBackgroundManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.forecastPager.adapter = VpAdapter(this, listOf(HoursFragment.newInstance(), DaysFragment.newInstance()))
        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.forecastPager) { tab, position ->
            tab.setText(if (position == 0) R.string.forecast_hours else R.string.forecast_days)
        }.also { it.attach() }

        backgroundManager = WeatherBackgroundManager(binding.weatherBackgroundPrimary, binding.weatherBackgroundSecondary, binding.weatherBackgroundScrim)
        model.liveDataWeatherIcon.observe(viewLifecycleOwner) { state ->
            backgroundManager.applyBackground(state)
        }
    }

    override fun onDestroyView() {
        backgroundManager.release()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        _binding?.forecastPager?.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic fun newInstance() = ForecastFragment()
    }
}
