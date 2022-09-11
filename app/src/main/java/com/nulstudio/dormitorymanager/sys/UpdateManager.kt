package com.nulstudio.dormitorymanager.sys

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.exception.NulRuntimeException
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import com.nulstudio.dormitorymanager.util.Base64
import java.io.File
import java.net.URL

object UpdateManager {

    private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/update.php"

    const val NDM_UPD_CHANNEL_RELEASE = 0
    const val NDM_UPD_CHANNEL_RC = 1
    const val NDM_UPD_CHANNEL_BETA = 2
    const val NDM_UPD_CHANNEL_ALPHA = 3
    const val NDM_UPD_CHANNEL_INSIDER_PREVIEW = 4
    const val NDM_UPD_CHANNEL_TECHNICAL_PREVIEW = 5
    const val NDM_UPD_CHANNEL_DEVELOPMENT = 6

    data class Release(var build: Int, var releaseLevel: Int, var updateUrl: String,
                       var versionString: String, var size: Long, var description: String)

    var lastRelease: Release? = null
        private set

    var isInitialized: Boolean = false
        private set

    var channel: Int = -1

    fun readUpdateSettings(context: Context) {
        val preferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.key_preference_file),
            Context.MODE_PRIVATE
        )

        channel = preferences.getInt("updateChannel", -1)

        isInitialized = true
    }

    fun saveUpdateSettings(context: Context) {
        val preferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.key_preference_file),
            Context.MODE_PRIVATE
        )

        preferences.edit().apply {
            putInt("updateChannel", channel)
            apply()
        }
    }

    fun checkForUpdate(): Release {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_UPDATE_FETCH_UPDATE, RequestAttribute().apply {
            put("channel", if(channel >= 0) channel else 0)
        })
        val result = request.sendRequest(URL(serverAddress))
        val resultAttribute = result.resultAttribute ?: throw NulRuntimeException(result.errorMessage)

        lastRelease = Release(
            resultAttribute.getInt("build"),
            resultAttribute.getInt("release_level"),
            resultAttribute.getString("update_url"),
            resultAttribute.getString("version_string"),
            resultAttribute.getLong("size"),
            Base64.decode(resultAttribute.getString("description"))
        )

        return lastRelease!!
    }

    fun downloadUpdate(context: Context): Boolean {
        if(lastRelease == null) {
            return false
        }

        val request = DownloadManager.Request(Uri.parse(lastRelease!!.updateUrl))
        request.setAllowedOverRoaming(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setTitle(context.getString(R.string.txt_update_title))
        request.setDescription(String.format(context.getString(R.string.txt_update_content), lastRelease!!.versionString))
        request.setVisibleInDownloadsUi(true)
        request.setDestinationUri(
            Uri.fromFile(
                File(
                    context.getExternalFilesDir("update"),
                    "update-" + lastRelease!!.build + ".apk"
                )
            )
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = downloadManager.enqueue(request)

        val downloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("Range")
            override fun onReceive(context: Context, intent: Intent) {
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val intentInstall = Intent(Intent.ACTION_VIEW)
                            val file: File = File(
                                context.getExternalFilesDir("update"),
                                "update-" + lastRelease!!.build + ".apk"
                            )
                            val apkUri = FileProvider.getUriForFile(
                                context,
                                context.applicationContext.packageName,
                                file
                            )
                            intentInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intentInstall.setDataAndType(
                                apkUri,
                                "application/vnd.android.package-archive"
                            )
                            intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intentInstall)
                            cursor.close()
                        }
                        DownloadManager.STATUS_FAILED -> cursor.close()
                    }
                }
            }
        }

        context.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        return true
    }
}