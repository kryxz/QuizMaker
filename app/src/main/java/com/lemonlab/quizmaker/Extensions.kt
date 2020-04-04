package com.lemonlab.quizmaker

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.util.*

fun Activity.hideKeypad() =
    with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }


fun Activity.showKeypad() =
    with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }


fun Context.copyText(text: String) {

    val clip = android.content.ClipData.newPlainText("code", text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(clip)
    Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()

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

