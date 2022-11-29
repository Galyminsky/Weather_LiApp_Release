package com.protonmail.jobforandroid.weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.protonmail.jobforandroid.weather.databinding.ActivityMainBinding
import com.protonmail.jobforandroid.weather.fragments.MainFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance())
            .commit()
    }

}