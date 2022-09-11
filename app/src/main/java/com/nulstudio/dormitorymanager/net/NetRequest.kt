package com.nulstudio.dormitorymanager.net

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class NetRequest(private val url: URL) {
    fun post(data: String, timeout: Int = 15000): String {
        val bs = StringBuilder()
        val connection = url.openConnection()
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        val out = OutputStreamWriter(
            connection.getOutputStream(), StandardCharsets.UTF_8
        )
        out.write(data)
        out.flush()
        out.close()
        connection.connect()
        val `is` = connection.getInputStream()
        val buffer = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
        var l: String? = null
        while (buffer.readLine().also { l = it } != null) {
            bs.append(l)
        }
        return bs.toString()
    }

    fun post(data: ByteArray, timeout: Int = 15000): String {
        val bs = StringBuilder()
        val connection = url.openConnection()
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "multipart/form-data; charset=utf-8; boundary=" + Math.random().toString().substring(2))
        connection.getOutputStream().write(data)
        connection.connect()
        val `is` = connection.getInputStream()
        val buffer = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
        var l: String? = null
        while (buffer.readLine().also { l = it } != null) {
            bs.append(l)
        }
        return bs.toString()
    }

    operator fun get(data: String, timeout: Int = 15000): String {
        val bs = StringBuilder()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        val out = OutputStreamWriter(
            connection.outputStream, StandardCharsets.UTF_8
        )
        out.write(data)
        out.flush()
        out.close()
        connection.connect()
        val `is` = connection.inputStream
        val buffer = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
        var l: String? = null
        while (buffer.readLine().also { l = it } != null) {
            bs.append(l)
        }
        return bs.toString()
    }
}