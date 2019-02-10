package com.example.videostreamingexoplayerexe

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mCurrentPosition: Int = -1

    protected abstract fun clear()

    open fun onBind(position: Int){
        mCurrentPosition = position
        clear()
    }

}