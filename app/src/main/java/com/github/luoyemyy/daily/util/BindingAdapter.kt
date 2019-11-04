package com.github.luoyemyy.daily.util

import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.luoyemyy.daily.R


@BindingAdapter("daily_text")
fun dailyText(textView: TextView, hasDaily: Boolean) {

    if (hasDaily) {
        textView.setTextColor(textView.context.getColor(R.color.textSuccess))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    } else {
        textView.setTextColor(textView.context.getColor(R.color.textPrimary))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    }
}

@BindingAdapter("has_daily", "is_today")
fun dailyDay(imageView: ImageView, hasDaily: Boolean, isToday: Boolean) {

    if (isToday) {
        if (hasDaily) {
            imageView.setImageResource(R.drawable.ic_today_success)
        } else {
            imageView.setImageResource(R.drawable.ic_today_not_write)
        }
    } else {
        imageView.setImageDrawable(null)
    }
}

