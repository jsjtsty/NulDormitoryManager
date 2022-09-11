package com.nulstudio.dormitorymanager.util

import java.nio.charset.StandardCharsets
import java.util.Base64

object Base64 {
    fun encode(s: String): String {
        return Base64.getEncoder().encodeToString(s.toByteArray())
    }

    fun decode(s: String): String {
        return String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8)
    }
}