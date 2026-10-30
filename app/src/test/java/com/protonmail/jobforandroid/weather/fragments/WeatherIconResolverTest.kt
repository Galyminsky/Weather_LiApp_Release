package com.protonmail.jobforandroid.weather.fragments

import com.protonmail.jobforandroid.weather.model.WeatherIconState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherIconResolverTest {
    @Test
    fun clearConditionsUseDayOrNightArtwork() {
        assertEquals("day.svg", WeatherIconResolver.resolve(WeatherIconState(1000, true)))
        assertEquals("night.svg", WeatherIconResolver.resolve(WeatherIconState(1000, false)))
    }

    @Test
    fun partlyCloudyConditionsUseMatchingDayOrNightArtwork() {
        assertEquals("cloudy-day-1.svg", WeatherIconResolver.resolve(WeatherIconState(1003, true)))
        assertEquals("cloudy-night-1.svg", WeatherIconResolver.resolve(WeatherIconState(1003, false)))
    }

    @Test
    fun precipitationUsesMatchingCategoryArtwork() {
        assertEquals("rainy-2.svg", WeatherIconResolver.resolve(WeatherIconState(1063, true)))
        assertEquals("rainy-4.svg", WeatherIconResolver.resolve(WeatherIconState(1063, false)))
        assertEquals("rainy-6.svg", WeatherIconResolver.resolve(WeatherIconState(1195, true)))
        assertEquals("snowy-2.svg", WeatherIconResolver.resolve(WeatherIconState(1066, true)))
        assertEquals("snowy-6.svg", WeatherIconResolver.resolve(WeatherIconState(1225, false)))
        assertEquals("thunder.svg", WeatherIconResolver.resolve(WeatherIconState(1276, true)))
    }

    @Test
    fun unsupportedCodesAndMissingDayStateForClearReturnFallbackMarker() {
        assertNull(WeatherIconResolver.resolve(WeatherIconState(1012, true)))
        assertNull(WeatherIconResolver.resolve(WeatherIconState(1168, false)))
        assertNull(WeatherIconResolver.resolve(WeatherIconState(1000, null)))
        assertNull(WeatherIconResolver.resolve(WeatherIconState(-1, null)))
        assertNull(WeatherIconResolver.resolve(null))
    }
}
