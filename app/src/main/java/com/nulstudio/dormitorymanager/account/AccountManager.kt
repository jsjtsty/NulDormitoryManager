package com.nulstudio.dormitorymanager.account

import android.content.Context
import android.content.SharedPreferences
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.exception.LoginException
import com.nulstudio.dormitorymanager.exception.NulRuntimeException
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.ErrorCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import com.nulstudio.dormitorymanager.sys.UpdateManager
import com.nulstudio.dormitorymanager.util.Base64
import java.net.URL
import java.security.MessageDigest

object AccountManager {

    private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/account.php"

    var isLoggedIn: Boolean = false
        private set
    var userName: String = ""
        private set
    var nickName: String = ""
        private set
    var uid: Int = 0
        private set
    var passHash: String = ""
        private set
    var priority: Int = 0
        private set
    val priorityStringResId: Int
        get() {
            return when(priority) {
                0 -> R.string.txt_priority_banned
                1 -> R.string.txt_priority_user
                2 -> R.string.txt_priority_advanced_user
                3 -> R.string.txt_priority_admin
                else -> 0
            }
        }

    fun login(context: Context): Boolean {
        if(!readLoginInstance(context)) {
            return false
        }

        val build = context.packageManager.getPackageInfo(context.packageName, 0)
            .longVersionCode.toInt()

        val request = Request(uid, ActionCode.NDM_ACT_ACCOUNT_LOGIN, RequestAttribute().apply {
            put("pass_hash", passHash)
            put("build", build)
        })

        val result = request.sendRequest(URL(serverAddress))

        when(result.errorCode) {
            ErrorCode.NDM_ERR_OK -> {}
            ErrorCode.NDM_ERR_USER_NOT_EXIST -> throw LoginException(context.getString(R.string.err_user_not_exist))
            ErrorCode.NDM_ERR_WRONG_PASSWORD -> throw LoginException(context.getString(R.string.err_wrong_password))
            ErrorCode.NDM_ERR_USER_IS_BANNED -> throw LoginException(context.getString(R.string.err_user_is_banned))
            else -> throw NulRuntimeException(result.errorMessage)
        }

        val resultAttribute = result.resultAttribute!!

        this.isLoggedIn = true
        this.userName = Base64.decode(resultAttribute.getString("user_name"))
        this.nickName = Base64.decode(resultAttribute.getString("nick_name"))
        this.uid = resultAttribute.getInt("uid")
        this.priority = resultAttribute.getInt("priority")

        if(!UpdateManager.isInitialized) UpdateManager.readUpdateSettings(context)

        if(UpdateManager.channel == -1) {
            UpdateManager.channel = resultAttribute.getInt("default_update_channel")
            UpdateManager.saveUpdateSettings(context)
        }

        saveLoginInstance(context)
        return true
    }

    fun login(context: Context, userName: String, password: String): Boolean {
        val build = context.packageManager.getPackageInfo(context.packageName, 0)
            .longVersionCode.toInt()

        val request = Request(0, ActionCode.NDM_ACT_ACCOUNT_LOGIN, RequestAttribute().apply {
            put("user_name", Base64.encode(userName))
            put("pass_hash", hashPassword(password))
            put("build", build)
        })
        val result = request.sendRequest(URL(serverAddress))

        when(result.errorCode) {
            ErrorCode.NDM_ERR_OK -> {}
            ErrorCode.NDM_ERR_USER_NOT_EXIST -> throw LoginException(context.getString(R.string.err_user_not_exist))
            ErrorCode.NDM_ERR_WRONG_PASSWORD -> throw LoginException(context.getString(R.string.err_wrong_password))
            ErrorCode.NDM_ERR_USER_IS_BANNED -> throw LoginException(context.getString(R.string.err_user_is_banned))
            else -> throw NulRuntimeException(result.errorMessage)
        }

        val resultAttribute = result.resultAttribute!!

        this.isLoggedIn = true
        this.userName = userName
        this.nickName = Base64.decode(resultAttribute.getString("nick_name"))
        this.uid = resultAttribute.getInt("uid")
        this.passHash = hashPassword(password)
        this.priority = resultAttribute.getInt("priority")

        if(!UpdateManager.isInitialized) UpdateManager.readUpdateSettings(context)

        if(UpdateManager.channel == -1) {
            UpdateManager.channel = resultAttribute.getInt("default_update_channel")
            UpdateManager.saveUpdateSettings(context)
        }

        saveLoginInstance(context)
        return true
    }

    fun register(context: Context, userName: String, password: String, inviteCode: String): Boolean {
        val request = Request(0, ActionCode.NDM_ACT_ACCOUNT_REGISTER, RequestAttribute().apply {
            put("invite_code", inviteCode)
            put("user_name", Base64.encode(userName))
            put("nick_name", Base64.encode(userName))
            put("pass_hash", hashPassword(password))
        })

        val result = request.sendRequest(URL(serverAddress))

        if (result.errorCode != ErrorCode.NDM_ERR_OK) {
            when (result.errorCode) {
                ErrorCode.NDM_ERR_INVALID_INVITE_CODE -> throw LoginException(context.getString(R.string.err_invalid_invite_code))
                ErrorCode.NDM_ERR_INVITE_CODE_USED_UP -> throw LoginException(context.getString(R.string.err_invite_code_used_up))
                ErrorCode.NDM_ERR_INVITE_CODE_BLOCKED -> throw LoginException(context.getString(R.string.err_invite_code_blocked))
                ErrorCode.NDM_ERR_USER_ALREADY_EXIST -> throw LoginException(context.getString(R.string.err_user_already_exist))
                else -> throw NulRuntimeException(result.errorMessage)
            }
        }
        val resultAttribute = result.resultAttribute!!
        this.isLoggedIn = true
        this.userName = AccountManager.userName
        this.uid = resultAttribute.getInt("uid")
        this.priority = resultAttribute.getInt("priority")
        this.passHash = hashPassword(password)

        if(!UpdateManager.isInitialized) UpdateManager.readUpdateSettings(context)

        if(UpdateManager.channel == -1) {
            UpdateManager.channel = resultAttribute.getInt("default_update_channel")
            UpdateManager.saveUpdateSettings(context)
        }

        saveLoginInstance(context)

        return true
    }

    fun modifyUserName(context: Context, name: String) {
        val request = Request(uid, ActionCode.NDM_ACT_ACCOUNT_MODIFY_USERNAME, RequestAttribute().apply {
            put("user_name", Base64.encode(name))
        })

        val result = request.sendRequest(URL(serverAddress))
        if (result.errorCode != ErrorCode.NDM_ERR_OK) {
            when (result.errorCode) {
                ErrorCode.NDM_ERR_USER_ALREADY_EXIST -> throw LoginException(context.getString(R.string.err_user_already_exist))
                else -> throw NulRuntimeException(result.errorMessage)
            }
        }

        userName = name
        saveLoginInstance(context)
    }

    fun modifyNickname(context: Context, name: String) {
        val request = Request(uid, ActionCode.NDM_ACT_ACCOUNT_MODIFY_NICKNAME, RequestAttribute().apply {
            put("nick_name", Base64.encode(name))
        })

        val result = request.sendRequest(URL(serverAddress))
        if (result.errorCode != ErrorCode.NDM_ERR_OK) {
            when (result.errorCode) {
                else -> throw NulRuntimeException(result.errorMessage)
            }
        }

        nickName = name
        saveLoginInstance(context)
    }

    private fun saveLoginInstance(context: Context) {
        val preferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.key_preference_file),
            Context.MODE_PRIVATE
        )
        preferences.edit().apply {
            putString("passHash", passHash)
            putString("userName", userName)
            putInt("uid", uid)
            putInt("priority", priority)
            putBoolean("isLoginSaved", true)
            apply()
        }
    }

    private fun readLoginInstance(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.key_preference_file),
            Context.MODE_PRIVATE
        )

        val isLoginSaved = preferences.getBoolean("isLoginSaved", false)
        if (!isLoginSaved) {
            return false
        }

        userName = preferences.getString("userName", null) ?: return false

        passHash = preferences.getString("passHash", null) ?: return false

        uid = preferences.getInt("uid", 0)
        if (uid == 0) {
            return false
        }

        priority = preferences.getInt("priority", -1)
        if (priority == -1) {
            return false
        }

        return true
    }

    private fun hashPassword(content: String): String {
        val hash = MessageDigest.getInstance("MD5").digest(content.toByteArray())
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            var str = Integer.toHexString(b.toInt())
            if (b < 0x10) {
                str = "0$str"
            }
            hex.append(str.substring(str.length -2))
        }
        return hex.toString()
    }
}