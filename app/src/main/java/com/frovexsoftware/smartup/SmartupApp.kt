package com.frovexsoftware.smartup

import android.app.Application

class SmartupApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applySavedLocale(this)
    }
}