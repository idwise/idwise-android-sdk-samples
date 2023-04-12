package com.idwise.dynamic.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


class AppPreferences(context: Context) {

    private val JOURNEY_ID = "JOURNEY_ID"
    private val REFERENCE_NO = "REFERENCE_NO"

    private var sharedPreferences: SharedPreferences? =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun reset() {
        sharedPreferences?.run {
            this.edit().clear().apply();
        }
    }

    var journeyId: String
        get() = getStringDefValue(JOURNEY_ID, "")
        set(obj) = saveString(JOURNEY_ID, obj)

    var referenceNumber: String
        get() = getStringDefValue(REFERENCE_NO, "")
        set(obj) = saveString(REFERENCE_NO, obj)


    fun getString(key: String?): String? {
        return sharedPreferences!!.getString(key, null)
    }

    fun getStringDefValue(key: String?, defVal: String): String {
        return sharedPreferences!!.getString(key, defVal) ?: defVal
    }

    fun saveString(key: String?, value: String?) {
        sharedPreferences?.edit()?.run {
            this.putString(key, value)
        }?.apply()
    }

    fun getInt(key: String?): Int {
        return sharedPreferences!!.getInt(key, 0)
    }

    fun getIntDefValue(key: String?, defVal: Int): Int {
        return sharedPreferences!!.getInt(key, defVal)
    }

    fun saveInt(key: String?, value: Int) {
        sharedPreferences?.edit()?.run {
            this.putInt(key, value)
        }?.apply()
    }

    fun getLong(key: String?): Long {
        return sharedPreferences!!.getLong(key, 0)
    }

    fun getLongDefValue(key: String?, defVal: Long): Long {
        return sharedPreferences!!.getLong(key, defVal)
    }

    fun saveLong(key: String?, value: Long) {
        sharedPreferences?.edit()?.run {
            this.putLong(key, value)
        }?.apply()
    }


    fun getBoolean(key: String?): Boolean {
        return sharedPreferences!!.getBoolean(key, false)
    }

    fun getBooleanDefValue(key: String?, defVal: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, defVal)
    }

    fun saveBoolean(key: String?, value: Boolean) {
        sharedPreferences?.edit()?.run {
            this.putBoolean(key, value)
        }?.apply()
    }

    fun getFloat(key: String?): Float {
        return sharedPreferences!!.getFloat(key, 0f)
    }

    fun getFloatDefValue(key: String?, defVal: Float): Float {
        return sharedPreferences!!.getFloat(key, defVal)
    }

    fun saveFloat(key: String?, value: Float) {
        sharedPreferences?.edit()?.run {
            this.putFloat(key, value)
        }?.apply()
    }

    fun clear() {
        sharedPreferences?.run {
            this.edit().clear().apply()
        }
    }

    fun clearSpecific(mKey: String?) {
        sharedPreferences?.run {
            this.edit().remove(mKey).apply()
        }
    }
}