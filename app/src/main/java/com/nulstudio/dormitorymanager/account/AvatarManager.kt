package com.nulstudio.dormitorymanager.account

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*

object AvatarManager {
    private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/avatar.php"

    var avatarMap = mutableMapOf<Int, Bitmap?>()
        private set

    fun requestFetchAvatars(set: Set<Int>) {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_AVATAR_FETCH, RequestAttribute().apply {
            val jsonArray = JSONArray().apply {
                for(i in set) {
                    if(!avatarMap.containsKey(i)) {
                        put(i)
                    }
                }
            }
            if(jsonArray.length() == 0) return
            put("uids", jsonArray)
        })

        val result = request.sendRequest(URL(serverAddress))
        val resultAttribute = result.resultAttribute!!

        val jsonImages = resultAttribute.getJSONArray("imgs")
        for(i: Int in 0 until jsonImages.length()) {
            jsonImages.getJSONObject(i).run {
                if(getString("img") != "") {
                    val bytes = Base64.getDecoder().decode(getString("img"))
                    avatarMap[getInt("uid")] = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else {
                    avatarMap[getInt("uid")] = null
                }
            }
        }
    }

    fun setAvatar(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, baos)
        val data = baos.toByteArray();
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_AVATAR_SET, RequestAttribute().apply {
            val str = Base64.getEncoder().encodeToString(data)
            put("img_base64", str)
        })
        val result = request.sendRequest(URL(serverAddress))
        avatarMap[AccountManager.uid] = bitmap
    }
}