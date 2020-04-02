package com.lemonlab.quizmaker

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import java.util.*

fun Activity.hideKeypad() =
    with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }


fun Activity.showKeypad() =
    with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

fun Long.timeAsAString(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return "${calendar.get(Calendar.DATE)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(
        Calendar.YEAR
    ).toString().substring(
        2,
        4
    )}"
}

fun String.removeWhitespace(): String {
    var isFirstSpace = false
    val result = StringBuilder()
    for (char in this) {
        if (char != ' ' && char != '\n') {
            isFirstSpace = true
            result.append(char)
        } else if (isFirstSpace) {
            result.append(" ")
            isFirstSpace = false
        }
    }
    return result.toString()
}

