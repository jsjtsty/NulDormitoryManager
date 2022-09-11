package com.nulstudio.dormitorymanager.net

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder


class Request {
    val version: Int = 3
    var uid: Int = 0
        private set
    var action: Int
        private set
    var request: RequestAttribute?
        private set

    constructor(uid: Int, action: Int) {
        this.uid = uid
        this.action = action
        request = RequestAttribute()
    }

    constructor(uid: Int, action: Int, request: RequestAttribute?) {
        this.uid = uid
        this.action = action
        this.request = request
    }

    @Throws(IOException::class, JSONException::class)
    fun sendRequest(url: URL): Result {
        return sendRequest(url, 15000)
    }

    @Throws(IOException::class, JSONException::class)
    fun sendRequest(url: URL, timeout: Int): Result {
        val net = NetRequest(url)
        val root = JSONObject()
        root.put("version", version)
        root.put("uid", uid)
        root.put("action", action)
        if (request == null) {
            root.put("request", 0)
        } else {
            root.put("request", request?.toJsonObject())
        }
        val resStr = net.post(root.toString(), timeout)

        val res: JSONObject

        return try {
            res = JSONObject(resStr)
            Result(res)
        } catch(e: JSONException) {
            Result(resStr)
        }
    }
}
