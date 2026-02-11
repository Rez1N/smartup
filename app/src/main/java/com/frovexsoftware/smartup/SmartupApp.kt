package com.frovexsoftware.smartup

import android.app.Application
import com.frovexsoftware.smartup.util.LocaleHelper

class SmartupApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applySavedLocale(this)
    }
}