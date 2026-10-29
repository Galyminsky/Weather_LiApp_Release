package com.protonmail.jobforandroid.weather.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.adapters.VpAdapter
import com.protonmail.jobforandroid.weather.databinding.FragmentForecastBinding

class ForecastFragment : Fragment() {
    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.forecastPager.adapter = VpAdapter(this, listOf(HoursFragment.newInstance(), DaysFragment.newInstance()))
        TabLayoutMediator(binding.tabLayout, binding.forecastPager) { tab, position ->
            tab.setText(if (position == 0) R.string.forecast_hours else R.string.forecast_days)
        }.attach()
    }

    override fun onDestroyView() {
        binding.forecastPager.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic fun newInstance() = ForecastFragment()
    }
}
