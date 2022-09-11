package com.nulstudio.dormitorymanager.account

import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import java.net.URL

object CreditManager {
    private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/credit.php"

    var coins: Int = 0
        private set

    fun update() {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_CREDIT_FETCH)
        val result = request.sendRequest(URL(serverAddress))

        coins = result.resultAttribute?.getInt("credit") ?: 0
    }
}