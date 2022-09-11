package com.nulstudio.dormitorymanager.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.forum.ForumManager
import com.nulstudio.dormitorymanager.sys.NulApplication


class PostActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var buttonNext: Button
    private lateinit var buttonBack: ImageView
    private lateinit var buttonImage: ImageView

    private lateinit var textTitleCount: TextView
    private lateinit var textContentCount: TextView

    private lateinit var recyclerPictures: RecyclerView

    private lateinit var layoutPictures: ConstraintLayout
    private lateinit var scrollView: NestedScrollView

    private val recyclerAdapter = PictureGridAdapter()

    private var pictureCount: Int = 0

    private val handler = Handler.createAsync(Looper.getMainLooper())

    private inner class PictureGridHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageContent: ImageView = view.findViewById(R.id.img_post_picture_item)
        val imageClose: ImageView = view.findViewById(R.id.img_post_picture_close)
    }

    private inner class PictureGridAdapter(val pictures: MutableList<Bitmap> = mutableListOf())
        : RecyclerView.Adapter<PictureGridHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureGridHolder {
            val view = layoutInflater.inflate(R.layout.grid_item_post_picture, parent, false)
            return PictureGridHolder(view)
        }

        override fun onBindViewHolder(holder: PictureGridHolder, position: Int) {
            val picture = pictures[position]

            holder.apply {
                imageContent.setImageBitmap(picture)
                imageClose.setOnClickListener {
                    deleteImage(position)
                    --pictureCount
                    if(pictureCount == 0) {
                        recyclerPictures.visibility = View.GONE
                    }
                }
            }
        }

        fun appendImage(bitmap: Bitmap) {
            pictures.add(bitmap)
            notifyItemInserted(pictures.size - 1)
            notifyItemRangeChanged(pictures.size - 1, pictures.size);
        }

        fun deleteImage(position: Int) {
            pictures.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, pictures.size)
        }

        override fun getItemCount(): Int = pictures.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        editTitle = findViewById(R.id.edit_post_title)
        editContent = findViewById(R.id.edit_post_content)
        buttonNext = findViewById(R.id.btn_post_next)
        buttonBack = findViewById(R.id.btn_post_back)
        buttonImage = findViewById(R.id.img_post_insert_pic)
        textTitleCount = findViewById(R.id.tv_post_title_count)
        textContentCount = findViewById(R.id.tv_post_content_count)
        recyclerPictures = findViewById(R.id.recycler_post_pictures)
        layoutPictures = findViewById(R.id.layout_post_content_picture)
        scrollView = findViewById(R.id.scroll_post_content)

        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.orientation = RecyclerView.VERTICAL
        recyclerPictures.layoutManager = gridLayoutManager
        recyclerPictures.adapter = recyclerAdapter

        scrollView.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if(oldBottom > bottom)
                scrollView.scrollTo(0, scrollView.scrollY - bottom + oldBottom)
        }

        editTitle.addTextChangedListener {
            textTitleCount.text = String.format("%d/50", it?.length)
        }

        editContent.addTextChangedListener {
            textContentCount.text = String.format("%d/30000", it?.length)
        }

        buttonNext.setOnClickListener {
            NulApplication.executorService.submit {
                ForumManager.post(editTitle.text.toString(), editContent.text.toString(), listOf())
                handler.post {
                    setResult(0)
                    finish()
                }
            }
        }

        buttonImage.setOnClickListener {
            if(pictureCount == POST_MAX_PICTURES) {
                Toast.makeText(
                    this,
                    String.format(getString(R.string.txt_post_reach_limit), POST_MAX_PICTURES),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), ACTION_PERMISSION_READ_STORAGE
                )
            } else {
                startActivityForResult(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ).apply { type = "image/*" }, ACTION_SELECT_PICTURE
                )
            }
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTION_SELECT_PICTURE && resultCode == RESULT_OK) {
            val selectedImage: Uri = data?.data ?: return
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage))
            recyclerAdapter.appendImage(bitmap)
            if(pictureCount == 0) {
                recyclerPictures.visibility = View.VISIBLE
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val ACTION_PERMISSION_READ_STORAGE = 1
        private const val ACTION_SELECT_PICTURE = 2

        private const val POST_MAX_PICTURES = 9
    }
}