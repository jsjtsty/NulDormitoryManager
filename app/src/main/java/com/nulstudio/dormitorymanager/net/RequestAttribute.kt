package com.nulstudio.dormitorymanager.net

import org.json.JSONException
import org.json.JSONObject

class RequestAttribute {
    var jsonObject: JSONObject = JSONObject()

    @Throws(JSONException::class)
    fun put(name: String, value: Int): RequestAttribute {
        jsonObject.put(name, value)
        return this
    }

    @Throws(JSONException::class)
    fun put(name: String, value: Long): RequestAttribute {
        jsonObject.put(name, value)
        return this
    }

    @Throws(JSONException::class)
    fun put(name: String, value: Double): RequestAttribute {
        jsonObject.put(name, value)
        return this
    }

    @Throws(JSONException::class)
    fun put(name: String, value: Boolean): RequestAttribute {
        jsonObject.put(name, value)
        return this
    }

    @Throws(JSONException::class)
    fun put(name: String, value: Any?): RequestAttribute {
        jsonObject.put(name, value)
        return this
    }

    fun toJsonObject(): JSONObject {
        return jsonObject
    }

    override fun toString(): String {
        return jsonObject.toString()
    }

}