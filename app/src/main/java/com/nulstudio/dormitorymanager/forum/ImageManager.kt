package com.nulstudio.dormitorymanager.forum

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nulstudio.dormitorymanager.net.NetRequest
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.ByteBuffer

object ImageManager {
    private const val IMAGE_UPLOAD_PATH = "http://49.232.80.216/dorm_mgr/cgi/img.php"

    fun uploadImage(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream)
        val request = NetRequest(URL(IMAGE_UPLOAD_PATH))
        return request.post(stream.toByteArray())
    }

    fun getImage(uuid: String): Bitmap {
        val url = URL("http://49.232.80.216/dorm_mgr/img/$uuid.jpg")
        val stream = url.openStream()
        val bitmap = BitmapFactory.decodeStream(stream)
        stream.close()
        return bitmap
    }
}