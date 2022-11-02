package com.protonmail.jobforandroid.weather.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.adapters.WeatherAdapter
import com.protonmail.jobforandroid.weather.adapters.WeatherModel
import com.protonmail.jobforandroid.weather.databinding.FragmentHoursBinding

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }

    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter()
        rcView.adapter = adapter
        val list = listOf(
            WeatherModel(
                "", "", "12:00", "Sunny", "28",
                "", "", "", "","", ""
            ),
            WeatherModel(
                "", "", "14:00", "Sunny", "26",
                "", "", "", "","", ""
            ),
            WeatherModel(
                "", "", "16:00", "Sunny", "24",
                "", "", "", "","", ""
            ),
            WeatherModel(
                "", "", "18:00", "Sunny", "23",
                "", "", "", "","", ""
            ),
            WeatherModel(
                "", "", "20:00", "Sunny", "22",
                "", "", "", "","", ""
            ),

            )
        adapter.submitList(list)
    }

    companion object {

        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}