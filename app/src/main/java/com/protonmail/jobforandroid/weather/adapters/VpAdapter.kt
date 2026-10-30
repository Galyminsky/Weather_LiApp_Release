package com.protonmail.jobforandroid.weather.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class VpAdapter(host: Fragment, private val list: List<Fragment>) : FragmentStateAdapter(host) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}
