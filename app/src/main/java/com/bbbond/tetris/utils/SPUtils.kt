package com.bbbond.tetris.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by bbbond on 2017/6/23.
 */

object SPUtils {
    private val NAME: String = "tetris"

    fun put(context: Context, key: String, value: Any?) {
        if (value == null) return
        val sp: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        if (value is String) {
            sp.edit().putString(key, value).apply()
        } else if (value is Boolean) {
            sp.edit().putBoolean(key, value).apply()
        } else if (value is Int) {
            sp.edit().putInt(key, value).apply()
        } else if (value is Float) {
            sp.edit().putFloat(key, value).apply()
        } else if (value is Long) {
            sp.edit().putLong(key, value).apply()
        } else {
            sp.edit().putString(key, value.toString()).apply()
        }
    }

    fun get(context: Context, key: String, default: Any): Any? {
        val sp: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        if (default is String) {
            return sp.getString(key, default)
        } else if (default is Boolean) {
            return sp.getBoolean(key, default)
        } else if (default is Int) {
            return sp.getInt(key, default)
        } else if (default is Float) {
            return sp.getFloat(key, default)
        } else if (default is Long) {
            return sp.getLong(key, default)
        } else {
            return sp.getString(key, "")
        }
    }

    fun remove(context: Context, key: String) {
        val sp: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        sp.edit().remove(key).apply()
    }

    fun clear(context: Context) {
        val sp: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    fun contains(context: Context, key: String): Boolean {
        val sp: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.contains(key)
    }
}
