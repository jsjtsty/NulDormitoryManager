package com.nulstudio.dormitorymanager.net

import org.json.JSONObject


class Result {
    var version: Int = 3
        private set
    var status: Int = 0
        private set
    var errorCode: Int = 0
        private set
    var errorMessage: String = ""
        private set
    var resultAttribute: ResultAttribute? = null
        private set

    private var origin: JSONObject

    override fun toString(): String {
        return origin.toString()
    }

    constructor(rawErrorMessage: String) {
        errorCode = ErrorCode.NDM_ERR_SCRIPT_ERROR
        errorMessage = rawErrorMessage
        origin = JSONObject()
    }

    constructor(res: JSONObject) {
        version = res.getInt("version")
        status = res.getInt("status")
        errorCode = res.getInt("err_code")
        errorMessage = res.getString("err_msg")
        if (!res.isNull("res")) {
            resultAttribute = ResultAttribute(res.getJSONObject("res"))
        }
        origin = res
    }
}