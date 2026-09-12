package com.pongploydev.priyankvex.paintapp.utils

import android.content.Context
import android.content.res.Configuration

object ScreenUtils {

    @JvmStatic
    fun isTablet(context: Context): Boolean {
        val screenLayout = context.resources.configuration.screenLayout
        val screenWidthDp = context.resources.configuration.screenWidthDp
        return (screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
                || screenWidthDp >= 600
    }

    @JvmStatic
    @JvmOverloads
    fun getGridSpanCount(
        context: Context,
        defaultPhoneSpan: Int = 2,
        tabletSpan: Int = 3,
        largeTabletSpan: Int = 4
    ): Int {
        val screenWidthDp = context.resources.configuration.screenWidthDp
        return when {
            screenWidthDp >= 840 -> largeTabletSpan
            screenWidthDp >= 600 -> tabletSpan
            else -> defaultPhoneSpan
        }
    }
}
