package com.example.finalproject

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object PrefsHelper {
    fun applySavedTheme(context: Context) {
        val mode = context.getSharedPreferences("rate_my_recipe_prefs", 0).getString("theme", "LIGHT")
        val nightMode = if (mode == "DARK")
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun saveTheme(context: Context, dark: Boolean) =
        context.getSharedPreferences("rate_my_recipe_prefs", 0).edit().putString("theme", if (dark) "DARK" else "LIGHT").apply()

    fun saveLastRecipe(context: Context, id: String) =
        context.getSharedPreferences("rate_my_recipe_prefs", 0).edit().putString("last_recipe_id", id).apply()

    fun getLastRecipe(ctx: Context): String? {
        return ctx.getSharedPreferences("rate_my_recipe_prefs", 0).getString("last_recipe_id", null)
    }
}