package com.nulstudio.dormitorymanager.account
import android.util.Log
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import java.lang.Exception
import java.net.URL


class ExpManager(val uid: Int) {
    companion object {
        private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/exp.php"

        lateinit var CurrentUserExpManager: ExpManager

        fun updateCurrentUser() {
            if(!this::CurrentUserExpManager.isInitialized) {
                CurrentUserExpManager = ExpManager(AccountManager.uid)
            }

            CurrentUserExpManager.update()
        }
    }

    private val expDemandArray = arrayOf(0, 20, 60, 120, 200, 400, 800, 1300, 2000, 3500, 7500, 15000, 50000,
                                    120000, 800000, 5200000, Int.MAX_VALUE)

    var exp: Int = 0
        private set(value) {
            field = value

            for(i in 1 until expDemandArray.size) {
                if(value < expDemandArray[i]) {
                    level = i - 1
                    break
                }
            }

            currentLevelDemand = expDemandArray[level + 1] - expDemandArray[level]
            currentLevelExp = value - expDemandArray[level]
        }

    var level: Int = 0
        private set

    var currentLevelDemand: Int = 0
        private set

    var currentLevelExp: Int = 0
        private set

    fun update() {
        try {
            val request =
                Request(AccountManager.uid, ActionCode.NDM_ACT_EXP_FETCH, RequestAttribute().apply {
                    put("uid", uid)
                })

            val result = request.sendRequest(URL(serverAddress))
            exp = result.resultAttribute!!.getInt("exp")
        } catch(e: Exception) {
            Log.e("ndm_err", e.stackTraceToString())
        }
    }
}