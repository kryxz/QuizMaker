package com.lemonlab.quizmaker.data

enum class Option {
    CACHE, FAQ, APPS, LOGOUT, ABOUT, PRIVACY, THEME, FEEDBACK
}

data class OptionsItem(
    val icon: Int,
    val text: String,
    val type: Option
)
