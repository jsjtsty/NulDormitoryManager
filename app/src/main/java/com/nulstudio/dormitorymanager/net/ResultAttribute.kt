package com.nulstudio.dormitorymanager.net

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ResultAttribute(var jsonObject: JSONObject) {
    @Throws(JSONException::class)
    fun getString(name: String): String {
        return jsonObject.getString(name)
    }

    @Throws(JSONException::class)
    fun getInt(name: String): Int {
        return jsonObject.getInt(name)
    }

    @Throws(JSONException::class)
    fun getLong(name: String): Long {
        return jsonObject.getLong(name)
    }

    @Throws(JSONException::class)
    fun getDouble(name: String): Double {
        return jsonObject.getDouble(name)
    }

    @Throws(JSONException::class)
    fun getBoolean(name: String): Boolean {
        return jsonObject.getBoolean(name)
    }

    @Throws(JSONException::class)
    fun getJSONArray(name: String): JSONArray {
        return jsonObject.getJSONArray(name)
    }

    @Throws(JSONException::class)
    fun getObject(name: String): JSONObject {
        return jsonObject.getJSONObject(name)
    }

    override fun toString(): String {
        return jsonObject.toString()
    }
}