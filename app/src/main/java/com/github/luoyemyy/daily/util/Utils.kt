package com.github.luoyemyy.daily.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.github.luoyemyy.daily.db.entity.Record

fun formatDateNum(n: Int): String {
    return if (n < 10) "0$n" else "$n"
}

fun formatDate(y: Int, m: Int, d: Int): String {
    return "$y-${formatDateNum(m)}-${formatDateNum(d)}"
}

fun formatDate(record: Record): String {
    return formatDate(record.year, record.month, record.day)
}


fun setToolbarTitle(activity: Activity, title: String?) {
    (activity as? AppCompatActivity)?.supportActionBar?.title = title
}

fun showSoftInput(context: Context, view: View) {
    context.getSystemService(InputMethodManager::class.java)?.apply {
        showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}