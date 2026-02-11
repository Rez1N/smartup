package com.frovexsoftware.smartup.util

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.frovexsoftware.smartup.R

/**
 * Shared time formatting logic used by both MainActivity and EditAlarmActivity.
 */
object TimeFormatter {

    fun format(context: Context, hour24: Int, minute: Int, is24h: Boolean): CharSequence {
        return if (is24h) {
            "%02d:%02d".format(hour24, minute)
        } else {
            val hour12 = ((hour24 + 11) % 12) + 1
            val suffix = if (hour24 >= 12) context.getString(R.string.time_pm) else context.getString(R.string.time_am)
            val text = "%d:%02d %s".format(hour12, minute, suffix)
            val spannable = SpannableString(text)
            val suffixStart = text.lastIndexOf(' ') + 1
            if (suffixStart in 1 until text.length) {
                spannable.setSpan(RelativeSizeSpan(0.7f), suffixStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(context.getColor(R.color.purple_medium)), suffixStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            spannable
        }
    }
}
