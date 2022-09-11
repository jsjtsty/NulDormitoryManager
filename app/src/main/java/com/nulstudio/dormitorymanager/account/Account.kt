package com.nulstudio.dormitorymanager.account

import android.graphics.Bitmap
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import com.nulstudio.dormitorymanager.util.Base64
import java.net.URL

class Account(uid: Int) {
    companion object {
        private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/account.php"
    }

    var userName: String = ""
        private set
    var nickName: String = ""
        private set
    var uid: Int = uid
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
    var avatarBitmap: Bitmap? = null
        private set

    fun update() {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_ACCOUNT_FETCH, RequestAttribute().apply {
            put("uid", uid)
        })
        val result = request.sendRequest(URL(serverAddress))

        val resultAttribute = result.resultAttribute!!

        this.userName = Base64.decode(resultAttribute.getString("user_name"))
        this.nickName = Base64.decode(resultAttribute.getString("nick_name"))
        this.uid = resultAttribute.getInt("uid")
        this.priority = resultAttribute.getInt("priority")

        if(!AvatarManager.avatarMap.containsKey(uid)) {
            AvatarManager.requestFetchAvatars(setOf(uid))
        }
        avatarBitmap = AvatarManager.avatarMap[uid]
    }

}