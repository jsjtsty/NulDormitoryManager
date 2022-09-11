package com.nulstudio.dormitorymanager.forum

import android.graphics.Bitmap
import android.media.Image
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.DormitoryManager
import com.nulstudio.dormitorymanager.exception.NulRuntimeException
import com.nulstudio.dormitorymanager.net.ActionCode
import com.nulstudio.dormitorymanager.net.Request
import com.nulstudio.dormitorymanager.net.RequestAttribute
import com.nulstudio.dormitorymanager.net.ResultAttribute
import com.nulstudio.dormitorymanager.util.Base64
import org.json.JSONArray
import java.lang.Math.min
import java.lang.StringBuilder
import java.net.URL
import java.util.*

object ForumManager {
    private const val serverAddress = "http://49.232.80.216/dorm_mgr/cgi/forum.php"

    data class Post(val id: Int, val uid: Int, val dormitoryId: Int, val userName: String,
                    val nickName: String,
                    val title: String, val content: String, val binaryBase64: String,
                    val status: Int, val visibility: Int,
                    val time: Date, val lastUpdate: Date, val viewCount: Int,
                    val commentCount: Int, val image: List<Bitmap>)

    data class Comment(val id: Int, val uid: Int, val mainId: Int, val userName: String,
                       val nickName: String,
                       val content: String, val binaryBase64: String,
                       val status: Int, val visibility: Int, val time: Date,
                       val floor: Int, val image: Bitmap?)

    class MainPost(val post: Post, val commentList: MutableList<Comment>) {
        fun sortByTime(reverse: Boolean = false): List<Comment> {
            if(!reverse) {
                commentList.sortBy { comment -> comment.time }
            } else {
                commentList.sortByDescending { comment -> comment.time }
            }
            return commentList
        }

        val landlordOnlyCommentList: List<Comment>
            get() {
                val list = mutableListOf<Comment>()
                for(v: Comment in commentList) {
                    if(v.uid == post.uid) {
                        list.add(v)
                    }
                }
                return list
            }
    }

    fun listPosts(startPos: Int = 0, limit: Int = 50): List<Post> {
        val postList = mutableListOf<Post>()
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_FORUM_LIST_POSTS, RequestAttribute().apply {
            put("dorm_id", DormitoryManager.id)
            put("start_pos", startPos)
            put("limit", limit)
        })
        val result = request.sendRequest(URL(serverAddress))
        val attribute: ResultAttribute = result.resultAttribute ?: throw NulRuntimeException(result.errorMessage)

        val array: JSONArray = attribute.getJSONArray("posts")
        for(i: Int in 0 until array.length()) {
            array.getJSONObject(i).run {
                /*
                val imageArray = getJSONArray("image")
                val list = mutableListOf<Bitmap>()
                for(j: Int in 0 until kotlin.math.min(imageArray.length(), 1)) {
                    if(imageArray.getString(j).isNotEmpty())
                        list.add(ImageManager.getImage(imageArray.getString(j)))
                }
                 */
                postList.add(
                    Post(
                        getInt("id"),
                        getInt("uid"),
                        getInt("dorm_id"),
                        Base64.decode(getJSONObject("name").getString("user_name")),
                        Base64.decode(getJSONObject("name").getString("nick_name")),
                        Base64.decode(getString("title")),
                        Base64.decode(getString("summary")) +
                                if(getBoolean("summaryAvail")) "..."
                                else "",
                        getString("binary"),
                        getInt("status"),
                        getInt("visibility"),
                        Date(getInt("time") * 1000L),
                        Date(getInt("last_update") * 1000L),
                        getInt("view_count"),
                        getInt("comment_count"),
                        listOf()
                    )
                )
            }
        }

        return postList
    }

    fun deletePost(id: Int) {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_FORUM_DELETE_POST, RequestAttribute().apply {
            put("id", id)
        })
        val result = request.sendRequest(URL(serverAddress))
    }

    fun deleteComment(mainId: Int, id: Int) {
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_FORUM_DELETE_COMMENT, RequestAttribute().apply {
            put("main_id", mainId)
            put("id", id)
        })
        val result = request.sendRequest(URL(serverAddress))
    }

    fun fetchPost(id: Int): MainPost {
        val commentList: MutableList<Comment> = mutableListOf()
        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_FORUM_FETCH_POST, RequestAttribute().apply {
            put("id", id)
        })

        val result = request.sendRequest(URL(serverAddress))
        val attribute: ResultAttribute = result.resultAttribute ?: throw NulRuntimeException(result.errorMessage)

        val post: Post

        attribute.run {
            val imageArray = getJSONArray("image")
            val list = mutableListOf<Bitmap>()
            for(j: Int in 0 until imageArray.length()) {
                if(imageArray.getString(j).isNotEmpty())
                    list.add(ImageManager.getImage(imageArray.getString(j)))
            }

            post = Post(
                getInt("id"),
                getInt("uid"),
                getInt("dorm_id"),
                Base64.decode(getObject("name").getString("user_name")),
                Base64.decode(getObject("name").getString("nick_name")),
                Base64.decode(getString("title")),
                Base64.decode(getString("content")),
                getString("binary"),
                getInt("status"),
                getInt("visibility"),
                Date(getInt("time") * 1000L),
                Date(getInt("last_update") * 1000L),
                getInt("view_count"),
                getInt("comment_count"),
                list
            )
        }

        val array: JSONArray = attribute.getJSONArray("comments")
        for(i: Int in 0 until array.length()) {
            array.getJSONObject(i).run {
                val bitmap: Bitmap? = getString("image").run {
                    if (this.isEmpty())
                        null
                    else
                        ImageManager.getImage(this)
                }
                commentList.add(
                    Comment(
                        getInt("id"),
                        getInt("uid"),
                        getInt("main_id"),
                        Base64.decode(getJSONObject("user").getString("user_name")),
                        Base64.decode(getJSONObject("user").getString("nick_name")),
                        Base64.decode(getString("content")),
                        getString("binary"),
                        getInt("status"),
                        getInt("visibility"),
                        Date(getInt("time") * 1000L),
                        getInt("floor"),
                        bitmap
                    )
                )
            }
        }

        return MainPost(post, commentList)
    }

    fun post(title: String, content: String, imageList: List<Bitmap> = listOf()): Int {
        val imageIdBuilder = StringBuilder()
        for(image: Bitmap in imageList) {
            val id = ImageManager.uploadImage(image)
            if(imageIdBuilder.isNotEmpty()) {
                imageIdBuilder.append(';')
            }
            imageIdBuilder.append(id)
        }

        val request = Request(AccountManager.uid, ActionCode.NDM_ACT_FORUM_POST, RequestAttribute().apply {
            put("title", Base64.encode(title))
            put("dorm_id", DormitoryManager.id)
            put("content", Base64.encode(content))
            put("summary", if(content.length > 32) Base64.encode(content.substring(0..31)) else "")
            put("binary", "")
            put("image", imageIdBuilder.toString())
        })

        val result = request.sendRequest(URL(serverAddress))
        val attribute: ResultAttribute = result.resultAttribute ?: throw NulRuntimeException(result.errorMessage)

        return attribute.getInt("id")
    }

    fun comment(mainId: Int, content: String, image: Bitmap? = null): Int {
        val imageId =
            if (image != null) {
                ImageManager.uploadImage(image)
            } else ""

        val request =
            Request(AccountManager.uid, ActionCode.NDM_ACT_FORUM_COMMENT, RequestAttribute().apply {
                put("main_id", mainId)
                put("content", Base64.encode(content))
                put("binary", "")
                put("image", imageId)
            })

        val result = request.sendRequest(URL(serverAddress))
        val attribute: ResultAttribute =
            result.resultAttribute ?: throw NulRuntimeException(result.errorMessage)

        return attribute.getInt("id")
    }
}