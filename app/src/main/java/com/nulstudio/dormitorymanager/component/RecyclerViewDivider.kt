package com.nulstudio.dormitorymanager.component

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class RecycleViewDivider(context: Context) : RecyclerView.ItemDecoration() {
    private var dividerHeight = 12

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = dividerHeight
    }
}
