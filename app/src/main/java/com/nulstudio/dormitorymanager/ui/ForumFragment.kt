package com.nulstudio.dormitorymanager.ui

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.AvatarManager
import com.nulstudio.dormitorymanager.component.RecycleViewDivider
import com.nulstudio.dormitorymanager.forum.ForumManager
import com.nulstudio.dormitorymanager.sys.NulApplication
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ForumFragment : Fragment() {

    private lateinit var layoutRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var buttonPost: ImageView
    private var list: List<ForumManager.Post> = listOf()

    private val handler = Handler.createAsync(Looper.getMainLooper())

    private lateinit var activity: MainPageActivity

    private val itemListener = object: OnItemClickListener {
        override fun onClick(position: Int, post: ForumManager.Post) {
            startActivity(Intent(activity, PostDetailActivity::class.java).apply {
                putExtra("id", post.id)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    private fun refresh() {
        list = ForumManager.listPosts(0, 1000)
        val set = mutableSetOf<Int>()
        for(item in list) {
            set.add(item.uid)
        }
        AvatarManager.requestFetchAvatars(set)

        Log.e("nul_drm", AvatarManager.avatarMap.toString())

        handler.post {
            val adapter = ForumAdapter(list)
            adapter.listener = itemListener
            recycler.adapter = adapter
            layoutRefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forum, container, false)

        layoutRefresh = view.findViewById(R.id.layout_forum_refresh)

        activity = requireActivity() as MainPageActivity

        recycler = view.findViewById(R.id.list_forum)
        recycler.layoutManager = LinearLayoutManager(activity)

        buttonPost = view.findViewById(R.id.btn_main_page_post)

        buttonPost.setOnClickListener {
            startActivityForResult(Intent(activity, PostActivity::class.java), 0)
        }

        layoutRefresh.setColorSchemeResources(R.color.btn_primary, R.color.purple_700, R.color.light_blue)

        NulApplication.executorService.submit {
            refresh()
        }

        layoutRefresh.setOnRefreshListener {
            NulApplication.executorService.submit {
                refresh()
            }
        }
        
        recycler.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val topRowVerticalPosition =
                    if (recyclerView.childCount == 0) 0 else recyclerView.getChildAt(0).top
                layoutRefresh.isEnabled = topRowVerticalPosition >= 0
            }
        })

        recycler.addItemDecoration(RecycleViewDivider(activity))

        return view
    }

    private inner class ForumHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageAvatar: ImageView = view.findViewById(R.id.img_forum_item_avatar)
        val textUserName: TextView = view.findViewById(R.id.tv_forum_item_user_name)
        val textTime: TextView = view.findViewById(R.id.tv_forum_item_time)
        val textTitle: TextView = view.findViewById(R.id.tv_forum_item_title)
        val textViewContent: TextView = view.findViewById(R.id.tv_forum_item_content)
        val textComment: TextView = view.findViewById(R.id.tv_forum_item_comment)
        val textViewCount: TextView = view.findViewById(R.id.tv_forum_item_view)
        val layout: LinearLayout = view.findViewById(R.id.layout_list_item_forum)
    }

    private interface OnItemClickListener {
        fun onClick(position: Int, post: ForumManager.Post)
    }

    private inner class ForumAdapter(val posts: List<ForumManager.Post>): RecyclerView.Adapter<ForumHolder>() {
        var listener: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumHolder {
            val view = layoutInflater.inflate(R.layout.list_item_forum, parent, false)
            return ForumHolder(view)
        }

        override fun onBindViewHolder(holder: ForumHolder, position: Int) {
            val post = posts[position]

            holder.apply {
                textUserName.text = post.nickName
                textTitle.text = post.title
                textViewCount.text = post.viewCount.toString()
                textComment.text = post.commentCount.toString()
                textViewContent.text = post.content
                textTime.text =
                    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(post.time)

                val avatar = AvatarManager.avatarMap[post.uid]
                if(avatar != null)
                    imageAvatar.setImageBitmap(avatar)
                else
                    imageAvatar.setImageDrawable(AppCompatResources.getDrawable(activity,
                        R.drawable.ic_baseline_account_circle_24))

                val personalInformationListener: View.OnClickListener = View.OnClickListener {
                    startActivity(Intent(activity, PersonalInformationActivity::class.java).apply {
                        putExtra("uid", post.uid)
                    })
                }

                imageAvatar.setOnClickListener(personalInformationListener)
                textUserName.setOnClickListener(personalInformationListener)
                textTime.setOnClickListener(personalInformationListener)
            }

            holder.layout.setOnClickListener {
                listener?.onClick(position, posts[position])
            }
        }

        override fun getItemCount(): Int = posts.size
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0) {
            layoutRefresh.isRefreshing = true
            NulApplication.executorService.submit {
                list = ForumManager.listPosts(0, 1000)
                handler.post {
                    val adapter = ForumAdapter(list)
                    adapter.listener = itemListener
                    recycler.adapter = adapter
                    layoutRefresh.isRefreshing = false
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ForumFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}