package com.nulstudio.dormitorymanager.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.AvatarManager
import com.nulstudio.dormitorymanager.forum.ForumManager
import com.nulstudio.dormitorymanager.sys.NulApplication
import com.nulstudio.dormitorymanager.util.Base64
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class PostDetailActivity : AppCompatActivity() {
    private lateinit var buttonBack: ImageView
    private lateinit var buttonMore: ImageView
    private lateinit var imageAvatar: CircleImageView
    private lateinit var textName: TextView
    private lateinit var textPostTime: TextView
    private lateinit var textTitle: TextView
    private lateinit var textContent: TextView
    private lateinit var textViewCount: TextView
    private lateinit var textAllComments: TextView
    private lateinit var textLandlordOnly: TextView
    private lateinit var textLatest: TextView
    private lateinit var textEarliest: TextView
    private lateinit var editComment: EditText
    private lateinit var textCommentCount: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var layoutPictures: LinearLayout
    private lateinit var recyclerImages: RecyclerView

    private lateinit var mainPost: ForumManager.MainPost

    private var isAllComments: Boolean = true
    private var isLatest: Boolean = true

    private var colorSelected: Int = 0
    private var colorNormal: Int = 0

    private val handler = Handler.createAsync(Looper.getMainLooper())

    private val commentListener: OnItemLongClickListener = object : OnItemLongClickListener {
        override fun onLongClick(position: Int, comment: ForumManager.Comment) {
            if(comment.uid == AccountManager.uid || AccountManager.priority >= 3) {
                AlertDialog.Builder(this@PostDetailActivity)
                    .setTitle("删除评论")
                    .setMessage("确定要删除评论吗")
                    .setPositiveButton("确定") { _: DialogInterface, _: Int ->
                        NulApplication.executorService.submit {
                            ForumManager.deleteComment(mainPost.post.id, comment.id)
                            refreshWithoutThread()
                            handler.post {
                                Toast.makeText(this@PostDetailActivity, "删除完成", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("取消") { _: DialogInterface, _: Int ->

                    }
                    .show()
            }
        }
    }

    private var id: Int = 0

    private fun updateCommentAdapter() {
        val adapter = CommentsAdapter(if(isAllComments) mainPost.commentList else mainPost.landlordOnlyCommentList)

        adapter.listener = commentListener
        recycler.adapter = adapter
    }

    private fun refresh() {
        NulApplication.executorService.submit {
            mainPost = ForumManager.fetchPost(id)
            val set = mutableSetOf<Int>()
            for(comment in mainPost.commentList) {
                set.add(comment.uid)
            }
            AvatarManager.requestFetchAvatars(set.apply {
                add(mainPost.post.uid)
            })

            handler.post {
                textName.text = mainPost.post.nickName
                textTitle.text = mainPost.post.title
                textContent.text = mainPost.post.content
                textViewCount.text = mainPost.post.viewCount.toString()
                textCommentCount.text = mainPost.post.commentCount.toString()
                textPostTime.text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    .format(mainPost.post.time)

                imageAvatar.setOnClickListener {
                    startActivity(Intent(this, PersonalInformationActivity::class.java).apply {
                        putExtra("uid", mainPost.post.uid)
                    })
                }

                val adapter = CommentsAdapter(mainPost.commentList)
                adapter.listener = commentListener
                recycler.adapter = adapter

                if(mainPost.post.image.isNotEmpty()) {
                    layoutPictures.visibility = View.VISIBLE
                    val imagesAdapter = ImagesAdapter(mainPost.post.image)
                    recyclerImages.adapter = imagesAdapter
                }

                val avatar = AvatarManager.avatarMap[mainPost.post.uid]
                if(avatar != null)
                    imageAvatar.setImageBitmap(avatar)
                else
                    imageAvatar.setImageDrawable(
                        AppCompatResources.getDrawable(this@PostDetailActivity,
                            R.drawable.ic_baseline_account_circle_24))
            }
        }
    }

    private fun refreshWithoutThread() {
        mainPost = ForumManager.fetchPost(id)
        Log.d("nul_drm_refresh", mainPost.toString())
        handler.post {
            textName.text = mainPost.post.userName
            textTitle.text = mainPost.post.title
            textContent.text = mainPost.post.content
            textViewCount.text = mainPost.post.viewCount.toString()
            textCommentCount.text = mainPost.post.commentCount.toString()
            textPostTime.text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                .format(mainPost.post.time)

            val adapter = CommentsAdapter(mainPost.commentList)
            adapter.listener = commentListener
            recycler.adapter = adapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        id = intent.getIntExtra("id", 0)

        buttonBack = findViewById(R.id.btn_post_detail_back)
        buttonMore = findViewById(R.id.btn_post_detail_more)
        imageAvatar = findViewById(R.id.img_post_detail_avatar)
        textName = findViewById(R.id.tv_post_detail_post_name)
        textPostTime = findViewById(R.id.tv_post_detail_post_time)
        textTitle = findViewById(R.id.tv_post_detail_content_title)
        textContent = findViewById(R.id.tv_post_detail_content)
        textViewCount = findViewById(R.id.tv_post_detail_view_count)
        textAllComments = findViewById(R.id.tv_post_detail_all_comments)
        textLandlordOnly = findViewById(R.id.tv_post_detail_landlord_only)
        textLatest = findViewById(R.id.tv_post_detail_latest)
        textEarliest = findViewById(R.id.tv_post_detail_earliest)
        textCommentCount = findViewById(R.id.tv_post_detail_comment_count)
        editComment = findViewById(R.id.edit_post_detail_comment)
        layoutPictures = findViewById(R.id.layout_post_detail_pictures)
        recyclerImages = findViewById(R.id.recycler_post_detail_images)

        recycler = findViewById(R.id.recycler_post_detail_comments)
        recycler.isNestedScrollingEnabled = false

        colorNormal = Color.rgb(120,120,120)
        colorSelected = getColor(R.color.black)

        recycler.layoutManager = LinearLayoutManager(this)
        recyclerImages.layoutManager = GridLayoutManager(this, 1)

        registerForContextMenu(recycler)

        buttonBack.setOnClickListener {
            onBackPressed()
        }

        editComment.setOnClickListener {
            startActivityForResult(Intent(this, PostActivity::class.java).apply {
                putExtra("mainId", id)
                putExtra("userName", textName.text.toString())
                putExtra("isComment", true)
            }, 1)
        }

        buttonMore.setOnClickListener {
            showMainMenu()
        }

        textLatest.setOnClickListener {
            textEarliest.setTextColor(colorNormal)
            textLatest.setTextColor(colorSelected)

            mainPost.sortByTime(true)
            isLatest = true
            updateCommentAdapter()
        }

        textEarliest.setOnClickListener {
            textLatest.setTextColor(colorNormal)
            textEarliest.setTextColor(colorSelected)

            mainPost.sortByTime()
            isLatest = false
            updateCommentAdapter()
        }

        textLandlordOnly.setOnClickListener {
            textLandlordOnly.setTextColor(colorSelected)
            textAllComments.setTextColor(colorNormal)

            isAllComments = false
            updateCommentAdapter()
        }

        textAllComments.setOnClickListener {
            textAllComments.setTextColor(colorSelected)
            textLandlordOnly.setTextColor(colorNormal)

            isAllComments = true
            updateCommentAdapter()
        }

        refresh()
    }

    private fun showMainMenu() {
        val popupMenu = PopupMenu(this, buttonMore)
        popupMenu.menuInflater.inflate(R.menu.menu_post_detail, popupMenu.menu)

        if(mainPost.post.uid != AccountManager.uid && AccountManager.priority < 3) {
            popupMenu.menu.getItem(0).isVisible = false
        }

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menu_post_detail_delete -> {
                    NulApplication.executorService.submit {
                        ForumManager.deletePost(mainPost.post.id)
                        handler.post {
                            Toast.makeText(this, "删除完成", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                R.id.menu_post_detail_copy_address -> {
                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("address",
                        String.format("ndmp://%s", Base64.encode(mainPost.post.id.toString()))))
                    Toast.makeText(this, getString(R.string.txt_copy_done), Toast.LENGTH_SHORT).show()
                }
            }

            false
        }
        popupMenu.show()
    }



    private inner class ImagesHolder(val view: View): RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.img_post_detail_picture_item)
    }

    private inner class CommentsHolder(val view: View): RecyclerView.ViewHolder(view) {
        val imageAvatar: CircleImageView = view.findViewById(R.id.img_comments_item_avatar)
        val textUserName: TextView = view.findViewById(R.id.tv_comments_item_user_name)
        val textFloor: TextView = view.findViewById(R.id.tv_comments_item_floor)
        val textContent: TextView = view.findViewById(R.id.tv_comments_item_content)
        val textTime: TextView = view.findViewById(R.id.tv_comments_item_time)
        val imgComment: ImageView = view.findViewById(R.id.img_comments_item_comment)
        val textComment: TextView = view.findViewById(R.id.tv_comments_item_comment)
        val imgLike: ImageView = view.findViewById(R.id.img_comments_item_like)
        val textLike: TextView = view.findViewById(R.id.tv_comments_item_like)
        val layout: LinearLayout = view.findViewById(R.id.layout_list_item_comments)
        val imageLayout: ConstraintLayout = view.findViewById(R.id.layout_list_item_comments_image)
        val image: ImageView = view.findViewById(R.id.img_comments_item)
    }

    private interface OnItemLongClickListener {
        fun onLongClick(position: Int, comment: ForumManager.Comment)
    }

    private inner class ImagesAdapter(val pictures: List<Bitmap>): RecyclerView.Adapter<PostDetailActivity.ImagesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesHolder {
            val view = layoutInflater.inflate(R.layout.grid_item_post_detail_picture, parent, false)
            return ImagesHolder(view)
        }

        override fun onBindViewHolder(holder: ImagesHolder, position: Int) {
            val picture = pictures[position]

            holder.apply {
                image.setImageBitmap(picture)
            }
        }

        override fun getItemCount(): Int = pictures.size
    }


    private inner class CommentsAdapter(val comments: List<ForumManager.Comment>): RecyclerView.Adapter<PostDetailActivity.CommentsHolder>() {
        var listener: OnItemLongClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostDetailActivity.CommentsHolder {
            val view = layoutInflater.inflate(R.layout.list_item_comments, parent, false)
            return CommentsHolder(view)
        }

        override fun onBindViewHolder(holder: PostDetailActivity.CommentsHolder, position: Int) {
            val comment = comments[position]

            holder.apply {
                textUserName.text = comment.nickName
                textFloor.text = "${comment.floor}F"
                textContent.text = comment.content
                textTime.text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    .format(comment.time)

                val avatar = AvatarManager.avatarMap[comment.uid]
                if(avatar != null)
                    imageAvatar.setImageBitmap(avatar)
                else
                    imageAvatar.setImageDrawable(
                        AppCompatResources.getDrawable(this@PostDetailActivity,
                        R.drawable.ic_baseline_account_circle_24))

                if(comment.image != null) {
                    imageLayout.visibility = View.VISIBLE
                    image.setImageBitmap(comment.image)
                }

                val personalInformationListener: View.OnClickListener = View.OnClickListener {
                    startActivity(Intent(this@PostDetailActivity, PersonalInformationActivity::class.java).apply {
                        putExtra("uid", comment.uid)
                    })
                }

                imageAvatar.setOnClickListener(personalInformationListener)
                textUserName.setOnClickListener(personalInformationListener)
                textTime.setOnClickListener(personalInformationListener)

            }

            holder.layout.setOnLongClickListener {
                listener?.onLongClick(position, comments[position])
                false
            }

        }

        override fun getItemCount(): Int = comments.size
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refresh()
    }
}