package com.protonmail.jobforandroid.weather.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.RequestQueue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.protonmail.jobforandroid.weather.BuildConfig.API_KEY
import com.protonmail.jobforandroid.weather.DialogManager
import com.protonmail.jobforandroid.weather.R
import com.protonmail.jobforandroid.weather.adapters.VpAdapter
import com.protonmail.jobforandroid.weather.databinding.FragmentMainBinding
import com.protonmail.jobforandroid.weather.model.MainViewModel
import com.protonmail.jobforandroid.weather.model.TodayWeatherMetrics
import com.protonmail.jobforandroid.weather.model.WeatherModel
import com.protonmail.jobforandroid.weather.model.WeatherBackdropState
import com.protonmail.jobforandroid.weather.model.WeatherIconState
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import com.protonmail.jobforandroid.weather.util.WeatherPresentation


interface MainScreenActions {
    fun searchCity()
    fun refreshWeather()
}

class MainFragment : Fragment(), MainScreenActions {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var requestQueue: RequestQueue
    private var weatherRequest: StringRequest? = null
    private var locationCancellation: CancellationTokenSource? = null
    private val model: MainViewModel by activityViewModels()

    private var userSelectedCity: String? = null
    private var activeRequestId: Long = 0L

    private val pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        activity?.let { host ->
            Toast.makeText(host, WeatherPresentation.permissionMessage(host, granted), Toast.LENGTH_SHORT).show()
        }
        if (granted) checkLocation()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission()
        init()
    }

    override fun onResume() {
        super.onResume()
        userSelectedCity?.let {
            requestWeatherData(it)
        } ?: checkLocation()
    }

    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestQueue = Volley.newRequestQueue(requireContext().applicationContext)
        screenPager.adapter = VpAdapter(this@MainFragment, listOf(
            CurrentWeatherFragment.newInstance(),
            ForecastFragment.newInstance()
        ))
    }

    override fun searchCity() {
        DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {
            override fun onClick(name: String?) {
                val query = name?.trim().orEmpty()
                if (query.isNotEmpty()) {
                    userSelectedCity = query
                    requestWeatherData(query)
                }
            }
        })
    }

    override fun refreshWeather() {
        userSelectedCity?.let {
            requestWeatherData(it)
        } ?: checkLocation()
    }

    private fun checkLocation() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) return
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isLocationEnabled
    }

    private fun getLocation() {
        locationCancellation?.cancel()
        val ct = CancellationTokenSource()
        locationCancellation = ct
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnSuccessListener { location ->
                if (!isAdded) return@addOnSuccessListener
                if (location == null) {
                    showError(R.string.error_location_unavailable)
                } else {
                    if (userSelectedCity == null) {
                        requestWeatherData("${location.latitude},${location.longitude}")
                    }
                }
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener
                Log.e("Weather", "Unable to get current location", error)
                showError(R.string.error_location_failed)
            }

    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        weatherRequest?.cancel()
        val currentRequestId = ++activeRequestId
        val queryCity = city.trim()
        val url = Uri.parse("https://api.weatherapi.com/v1/forecast.json")
            .buildUpon()
            .appendQueryParameter("key", API_KEY)
            .appendQueryParameter("q", queryCity)
            .appendQueryParameter("days", "7")
            .appendQueryParameter("aqi", "no")
            .appendQueryParameter("alerts", "no")
            .appendQueryParameter("lang", "ru")
            .build()
        val request = object : StringRequest(
            Method.GET,
            url.toString(),
            { result ->
                if (currentRequestId == activeRequestId && isAdded) {
                    try {
                        Log.d("WeatherAPI", "Response: $result")
                        parseWeatherData(result, queryCity)
                    } catch (error: Exception) {
                        Log.e("Weather", "Unable to parse weather response", error)
                        showError(R.string.error_weather_parse)
                    }
                }
            },
            { error ->
                if (currentRequestId == activeRequestId && isAdded) {
                    Log.e("Weather", "Weather request failed", error)
                    showError(R.string.error_weather_load)
                }
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                val parsed = try {
                    String(response.data, java.nio.charset.StandardCharsets.UTF_8)
                } catch (_: Exception) {
                    String(response.data)
                }
                return com.android.volley.Response.success(parsed, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response))
            }
        }
        weatherRequest = request
        requestQueue.add(request)
    }

    private fun showError(messageRes: Int) {
        if (isAdded) Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    private fun parseWeatherData(result: String, queryCity: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject, queryCity)
        if (list.isEmpty()) throw IllegalStateException("Forecast response contains no days")
        parseCurrentData(mainObject, list[0], queryCity)
        updateWeatherBackground(mainObject)
    }

    private fun parseDays(mainObject: JSONObject, queryCity: String): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val country = mainObject.getJSONObject("location").getString("country")
        val region = mainObject.getJSONObject("location").getString("region")
        val rawName = mainObject.getJSONObject("location").getString("name")
        val name = WeatherPresentation.localizedCityName(rawName, queryCity)
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                country,
                region,
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString(),
                day.getJSONObject("astro").getString("sunrise"),
                day.getJSONObject("astro").getString("sunset"),
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }


    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel, queryCity: String) {
        val rawName = mainObject.getJSONObject("location").getString("name")
        val name = WeatherPresentation.localizedCityName(rawName, queryCity)
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("country"),
            mainObject.getJSONObject("location").getString("region"),
            name,
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours,
            weatherItem.sunrise,
            weatherItem.sunset,
        )
        model.liveDataToday.value = item
        model.liveDataCurrent.value = item

        val current = mainObject.optJSONObject("current")
        val day = mainObject.optJSONObject("forecast")
            ?.optJSONArray("forecastday")
            ?.optJSONObject(0)
            ?.optJSONObject("day")
        val rainChance = day?.optString("daily_chance_of_rain")?.toMetricNumber()
        val snowChance = day?.optString("daily_chance_of_snow")?.toMetricNumber()
        val precipitationChance = listOfNotNull(rainChance, snowChance).maxOrNull()
        model.liveDataTodayMetrics.value = TodayWeatherMetrics(
            feelsLikeC = current?.optString("feelslike_c")?.toMetricNumber(),
            windSpeedKph = current?.optString("wind_kph")?.toMetricNumber(),
            windDirection = current?.optString("wind_dir")?.takeIf { it.isNotBlank() && it != "null" },
            humidityPercent = current?.optString("humidity")?.toMetricNumber(),
            precipitationChancePercent = precipitationChance,
            pressureHpa = current?.optString("pressure_mb")?.toMetricNumber()
        )
        val currentWeather = mainObject.optJSONObject("current")
        val conditionCode = currentWeather?.optJSONObject("condition")?.optInt("code", -1) ?: -1
        val isDay = currentWeather?.optInt("is_day", -1)?.let { value ->
            when (value) {
                0 -> false
                1 -> true
                else -> null
            }
        }
        model.liveDataWeatherIcon.value = WeatherIconState(conditionCode, isDay)
    }

    private fun String?.toMetricNumber(): String? {
        val value = this?.toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
        return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().trimEnd('0').trimEnd('.')
    }

    private fun updateWeatherBackground(response: JSONObject) {
        val context = context ?: return
        val current = response.optJSONObject("current")
        val conditionCode = current
            ?.optJSONObject("condition")
            ?.optInt("code", -1)
            ?: -1
        val tintResource = when (conditionCode) {
            1000 -> R.color.weather_accent
            1003, 1006, 1009 -> R.color.weather_accent_secondary
            1063, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195,
            1198, 1201, 1240, 1243, 1246 -> R.color.weather_accent
            1066, 1069, 1072, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219,
            1222, 1225, 1237, 1249, 1252, 1255, 1258, 1261, 1264 ->
                R.color.weather_surface_elevated
            1087, 1273, 1276, 1279, 1282 -> R.color.weather_error
            1012, 1015, 1018, 1021, 1024, 1027, 1030, 1033, 1036, 1039, 1042,
            1045, 1048, 1135, 1147 -> R.color.weather_accent_secondary
            else -> null
        }

        if (tintResource == null) {
            val baseColor = context.getColor(R.color.weather_background)
            model.liveDataWeatherBackdrop.value = WeatherBackdropState(baseColor, baseColor)
            return
        }

        val location = response.optJSONObject("location")
        val forecastDay = response.optJSONObject("forecast")
            ?.optJSONArray("forecastday")
            ?.optJSONObject(0)
        val astronomy = forecastDay?.optJSONObject("astro")
        val sunrise = parseAstronomyTime(astronomy?.optString("sunrise"))
        val sunset = parseAstronomyTime(astronomy?.optString("sunset"))
        val cityLocalTime = parseLocationTime(location)
        val dayPhase = if (cityLocalTime != null && sunrise != null && sunset != null) {
            resolveAstronomicalDayPhase(cityLocalTime, sunrise, sunset)
                ?: resolveDeviceDayPhase(LocalTime.now())
        } else {
            resolveDeviceDayPhase(LocalTime.now())
        }
        val isDay = current?.optInt("is_day", -1)?.takeIf { it == 0 || it == 1 }
        val isNight = isDay == 0 || (isDay == null && dayPhase == DayPhase.NIGHT)

        val phaseAlpha = when (dayPhase) {
            DayPhase.MORNING -> 0.14f
            DayPhase.DAY -> 0.10f
            DayPhase.EVENING -> 0.17f
            DayPhase.NIGHT -> 0.08f
        }
        val tintAlpha = phaseAlpha * if (isNight) 0.72f else 1f
        val baseColor = context.getColor(R.color.weather_background)
        val tintColor = context.getColor(tintResource)
        val secondaryColor = context.getColor(R.color.weather_accent_secondary)
        val topColor = ColorUtils.blendARGB(baseColor, tintColor, tintAlpha)
        val bottomColor = ColorUtils.blendARGB(baseColor, secondaryColor, tintAlpha * 0.4f)
        model.liveDataWeatherBackdrop.value = WeatherBackdropState(topColor, bottomColor)
    }

    private fun parseLocationTime(location: JSONObject?): LocalTime? {
        val timeText = location?.optString("localtime")?.takeIf { it.isNotBlank() }
            ?: return null
        val timeZone = location.optString("tz_id").takeIf { it.isNotBlank() }
            ?: return null
        return runCatching {
            val zone = ZoneId.of(timeZone)
            LocalDateTime.parse(timeText, locationDateTimeFormatter)
                .atZone(zone)
                .toLocalTime()
        }.getOrNull()
    }

    private fun parseAstronomyTime(value: String?): LocalTime? {
        val timeText = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return runCatching {
            LocalTime.parse(timeText, astronomyTimeFormatter)
        }.getOrNull()
    }

    private fun resolveAstronomicalDayPhase(
        now: LocalTime,
        sunrise: LocalTime,
        sunset: LocalTime
    ): DayPhase? {
        if (!sunrise.isBefore(sunset)) return null
        val daylightMinutes = Duration.between(sunrise, sunset).toMinutes()
        if (daylightMinutes !in 1L..1440L) return null
        if (now.isBefore(sunrise) || !now.isBefore(sunset)) return DayPhase.NIGHT

        val morningMinutes = if (daylightMinutes < 360L) daylightMinutes / 2 else 180L
        val eveningMinutes = if (daylightMinutes < 360L) {
            daylightMinutes - morningMinutes
        } else {
            180L
        }
        val morningEnd = sunrise.plusMinutes(morningMinutes)
        val eveningStart = sunset.minusMinutes(eveningMinutes)
        return when {
            now.isBefore(morningEnd) -> DayPhase.MORNING
            now.isBefore(eveningStart) -> DayPhase.DAY
            else -> DayPhase.EVENING
        }
    }

    private fun resolveDeviceDayPhase(now: LocalTime): DayPhase = when (now.hour) {
        in 5..10 -> DayPhase.MORNING
        in 11..16 -> DayPhase.DAY
        in 17..20 -> DayPhase.EVENING
        else -> DayPhase.NIGHT
    }

    private enum class DayPhase {
        MORNING,
        DAY,
        EVENING,
        NIGHT
    }

    companion object {
        private val locationDateTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter(Locale.US)

        private val astronomyTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    override fun onDestroyView() {
        weatherRequest?.cancel()
        weatherRequest = null
        locationCancellation?.cancel()
        locationCancellation = null
        if (::requestQueue.isInitialized) requestQueue.stop()
        _binding?.screenPager?.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
