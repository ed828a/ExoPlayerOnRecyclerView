package com.example.videostreamingexoplayerexe

import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_empty_layout.view.*
import kotlinx.android.synthetic.main.item_layout.view.*

class VideoRecyclerViewAdapter(val mInfoList: List<VideoInfo>) : RecyclerView.Adapter<BaseViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            VIEW_TYPE_NORMAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
                return ViewHolder(view)
            }

            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty_layout, parent, false)
                return EmptyViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
                return ViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (mInfoList.isNotEmpty()) VIEW_TYPE_NORMAL
        else VIEW_TYPE_EMPTY


    override fun getItemCount(): Int = if (mInfoList.isNotEmpty()) mInfoList.size else 1


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        val textViewTitle = itemView.textViewTitle
        val userHandle = itemView.userHandle
        val videoLayout = itemView.videoLayout
        val cover = itemView.cover
        val progressBar = itemView.progressBar
        val parent = itemView

        override fun clear() {

        }


        override fun onBind(position: Int) {
            super.onBind(position)
            parent.tag = this@ViewHolder
            val videoInfo = mInfoList[position]
            textViewTitle.text = videoInfo.mTitle
            userHandle.text = videoInfo.mUserHandle
            Glide.with(itemView.context).load(videoInfo.mCoverUrl).apply(RequestOptions().optionalCenterCrop())
                .into(cover)
        }
    }

    inner class EmptyViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val retryButton = itemView.btn_retry
        val messageTextView = itemView.tv_message

        init {
            itemView.visibility = View.GONE
            itemView.btn_retry.setOnClickListener {
                Toast.makeText(it.context, "No retry function yet", Toast.LENGTH_SHORT).show()
            }
        }

        override fun clear() {

        }
    }

    companion object {
        @IntDef(VIEW_TYPE_EMPTY, VIEW_TYPE_NORMAL)
        @Retention(AnnotationRetention.SOURCE) // not store in Binary code
        annotation class ViewType

        const val VIEW_TYPE_EMPTY = 0
        const val VIEW_TYPE_NORMAL = 1


    }
}