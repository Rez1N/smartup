package com.frovexsoftware.smartup

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {
    private const val PREFS_NAME = "alarm_prefs"
    private const val KEY_LANG = "app_lang"

    fun wrap(base: Context): Context {
        return base
    }

    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, null)
    }

    fun saveLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, language)
            .apply()
    }

    fun clearLanguage(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LANG)
            .apply()
    }

    fun applySavedLocale(context: Context) {
        val saved = getSavedLanguage(context)
        setAppLocale(context, saved)
    }

    fun setAppLocale(context: Context, languageTag: String?) {
        if (languageTag.isNullOrBlank()) {
            clearLanguage(context)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            saveLanguage(context, languageTag)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        }
    }
}