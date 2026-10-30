package com.protonmail.jobforandroid.weather.util

import android.content.Context
import com.protonmail.jobforandroid.weather.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatting applied only when values are presented; API values remain unchanged. */
object WeatherPresentation {
    private val conditions = mapOf(
        "sunny" to "Солнечно",
        "clear" to "Ясно",
        "partly cloudy" to "Переменная облачность",
        "cloudy" to "Облачно",
        "overcast" to "Пасмурно",
        "mist" to "Дымка",
        "patchy rain possible" to "Местами возможен дождь",
        "patchy snow possible" to "Местами возможен снег",
        "patchy sleet possible" to "Местами возможен дождь со снегом",
        "patchy freezing drizzle possible" to "Местами возможна ледяная морось",
        "thundery outbreaks possible" to "Возможны грозы",
        "blowing snow" to "Метель",
        "blizzard" to "Сильная метель",
        "fog" to "Туман",
        "freezing fog" to "Ледяной туман",
        "patchy light drizzle" to "Местами слабая морось",
        "light drizzle" to "Слабая морось",
        "freezing drizzle" to "Ледяная морось",
        "heavy freezing drizzle" to "Сильная ледяная морось",
        "patchy light rain" to "Местами небольшой дождь",
        "light rain" to "Небольшой дождь",
        "moderate rain at times" to "Временами умеренный дождь",
        "moderate rain" to "Умеренный дождь",
        "heavy rain at times" to "Временами сильный дождь",
        "heavy rain" to "Сильный дождь",
        "light freezing rain" to "Небольшой ледяной дождь",
        "moderate or heavy freezing rain" to "Умеренный или сильный ледяной дождь",
        "light sleet" to "Небольшой дождь со снегом",
        "moderate or heavy sleet" to "Умеренный или сильный дождь со снегом",
        "patchy light snow" to "Местами небольшой снег",
        "light snow" to "Небольшой снег",
        "patchy moderate snow" to "Местами умеренный снег",
        "moderate snow" to "Умеренный снег",
        "patchy heavy snow" to "Местами сильный снег",
        "heavy snow" to "Сильный снег",
        "ice pellets" to "Ледяная крупа",
        "light rain shower" to "Небольшой дождь",
        "moderate or heavy rain shower" to "Умеренный или сильный ливень",
        "torrential rain shower" to "Очень сильный ливень",
        "light sleet showers" to "Небольшие осадки в виде дождя со снегом",
        "moderate or heavy sleet showers" to "Умеренные или сильные осадки в виде дождя со снегом",
        "light snow showers" to "Небольшой снегопад",
        "moderate or heavy snow showers" to "Умеренный или сильный снегопад",
        "light showers of ice pellets" to "Небольшие осадки в виде ледяной крупы",
        "moderate or heavy showers of ice pellets" to "Умеренные или сильные осадки в виде ледяной крупы",
        "patchy light rain with thunder" to "Местами небольшой дождь с грозой",
        "moderate or heavy rain with thunder" to "Умеренный или сильный дождь с грозой",
        "patchy light snow with thunder" to "Местами небольшой снег с грозой",
        "moderate or heavy snow with thunder" to "Умеренный или сильный снег с грозой"
    )

    private val ruDate = Locale.forLanguageTag("ru-RU")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", ruDate)

    private val cityNames = mapOf(
        "lisakovsk" to "Лисаковск",
        "moscow" to "Москва",
        "saint petersburg" to "Санкт-Петербург",
        "st petersburg" to "Санкт-Петербург",
        "astana" to "Астана",
        "nur-sultan" to "Астана",
        "almaty" to "Алматы",
        "shymkent" to "Шымкент",
        "karaganda" to "Караганда",
        "qostanay" to "Костанай",
        "kostanay" to "Костанай",
        "kustanay" to "Костанай",
        "aktobe" to "Актобе",
        "taraz" to "Тараз",
        "pavlodar" to "Павлодар",
        "semey" to "Семей",
        "ust-kamenogorsk" to "Усть-Каменогорск",
        "kyzylorda" to "Кызылорда",
        "ural'sk" to "Уральск",
        "uralsk" to "Уральск",
        "petropavl" to "Петропавловск",
        "petropavlovsk" to "Петропавловск",
        "aktau" to "Актау",
        "temirtau" to "Темиртау",
        "turkestan" to "Туркестан",
        "kokshetau" to "Кокшетау",
        "atyrau" to "Атырау",
        "ekibastuz" to "Экибастуз",
        "rudny" to "Рудный",
        "zhezkazgan" to "Жезказган"
    )

    fun localizedCityName(cityName: String, requestedQuery: String? = null): String {
        val query = requestedQuery?.trim().orEmpty()
        if (query.any { it in 'а'..'я' || it in 'А'..'Я' }) {
            return query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
        val lowerName = cityName.trim().lowercase(Locale.ROOT)
        return cityNames[lowerName] ?: cityName
    }

    fun condition(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return "Погодные условия неизвестны"
        return conditions[trimmed.lowercase(Locale.ROOT)] ?: trimmed
    }

    fun roundedTemperature(value: String?): String? {
        val text = value?.trim()?.takeIf(String::isNotEmpty) ?: return null
        return runCatching {
            BigDecimal(text).setScale(0, RoundingMode.HALF_UP).toPlainString()
        }.getOrNull()
    }

    fun forecastDate(value: String, hourly: Boolean): String {
        return runCatching {
            if (hourly) {
                val dateTime = LocalDateTime.parse(value.replace(' ', 'T'))
                dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm", ruDate))
            } else {
                LocalDate.parse(value).format(dayFormatter)
            }
        }.getOrElse { value }
    }

    fun localizedClockTime(value: String): String = runCatching {
        LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("h:mm a", Locale.US))
            .format(DateTimeFormatter.ofPattern("HH:mm", ruDate))
    }.getOrElse { value }

    fun permissionMessage(context: Context, granted: Boolean): String = context.getString(
        if (granted) R.string.permission_result_granted else R.string.permission_result_denied
    )
}
