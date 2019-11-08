package com.github.luoyemyy.daily.util

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.luoyemyy.daily.R


@BindingAdapter("daily_text", "is_today")
fun dailyText(textView: TextView, hasDaily: Boolean, isToday: Boolean) {

    if (hasDaily) {
        textView.setTextColor(textView.context.getColor(R.color.textSuccess))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    } else {
        textView.setTextColor(textView.context.getColor(R.color.textPrimary))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    }
    if (isToday) {
        if (hasDaily) {
            textView.setBackgroundResource(R.drawable.bg_today_yes)
        } else {
            textView.setBackgroundResource(R.drawable.bg_today_no)
        }
    } else {
        textView.background = null
    }
}

@BindingAdapter("visibility")
fun visibility(view: View, visible: Boolean) {
    view.visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}


