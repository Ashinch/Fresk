package com.ashinch.reader.utils

import android.content.Context

class DimensionUtil {
    companion object {
        fun px2dp(context: Context, value: Int): Int = (value / context.resources.displayMetrics.density + 0.5f).toInt()
        fun dp2px(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}