package com.nulstudio.dormitorymanager.account

import android.accounts.Account
import android.app.DownloadManager
import android.content.Context
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.exception.DormitoryManagerException
import com.nulstudio.dormitorymanager.exception.NulRuntimeException
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.ErrorCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import com.nulstudio.dormitorymanager.util.Base64
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.security.auth.login.LoginException

object DormitoryManager {

    private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/dorm_mgr.php"

    var isBound: Boolean = false
        private set
    var id: Int = 0
        private set
    var priority: Int = 0
        private set
    var name: String = ""
        private set
    var description: String = ""
        private set
    var code: String = ""
        private set

    data class Member(val uid: Int, val name: String, val priority: Int)

    var members: MutableList<Member> = mutableListOf()
        private set

    fun update() {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_DORM_QUERY)
        val result = request.sendRequest(URL(serverAddress))

        when(result.errorCode) {
            ErrorCode.NDM_ERR_OK -> {}
            else -> throw NulRuntimeException(result.errorMessage)
        }

        val resultAttribute = result.resultAttribute ?: throw NulRuntimeException(result.errorMessage)

        isBound = resultAttribute.getBoolean("is_bound")
        if(isBound) {
            members.clear()
            resultAttribute.run {
                id = getInt("id")
                priority = getInt("priority")
                name = Base64.decode(getString("name"))
                description = Base64.decode(getString("description"))
                code = getString("code")

                val jsonMembers = getJSONArray("members")
                for(i: Int in 0 until jsonMembers.length()) {
                    jsonMembers.getJSONObject(i).run {
                        members.add(Member(getInt("uid"), getString("name"), getInt("dorm_priority")))
                    }
                }
            }
        }
    }

    fun createDormitory(context: Context, name: String, description: String) {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_DORM_CREATE, RequestAttribute().apply {
            put("name", Base64.encode(name))
            put("description", Base64.encode(description))
        })
        val result = request.sendRequest(URL(serverAddress))

        when(result.errorCode) {
            ErrorCode.NDM_ERR_OK -> {}
            ErrorCode.NDM_ERR_DORM_ALREADY_EXIST -> throw DormitoryManagerException(context.getString(R.string.err_dorm_already_exist))
            else -> throw NulRuntimeException(result.errorMessage)
        }

        update()
    }

    fun joinDormitory(context: Context, code: String) {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_DORM_JOIN, RequestAttribute().apply {
            put("code", code)
        })
        val result = request.sendRequest(URL(serverAddress))

        when(result.errorCode) {
            ErrorCode.NDM_ERR_OK -> {}
            ErrorCode.NDM_ERR_DORM_CODE_NOT_EXIST -> throw DormitoryManagerException(context.getString(R.string.err_dorm_code_not_exist))
            else -> throw NulRuntimeException(result.errorMessage)
        }

        update()
    }
    
}