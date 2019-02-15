package com.example.videostreamingexoplayerexe

import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.item_layout.view.*

class ItemViewHolder (itemView: View,
                      private val videoSurfaceView: PlayerView,
                      private val player: SimpleExoPlayer,
                      private val InfoList: List<VideoInfo>) : BaseViewHolder(itemView) {

    val textViewTitle = itemView.textViewTitle
    val userHandle = itemView.userHandle
    val videoLayout = itemView.videoLayout
    val cover = itemView.cover
    val progressBar = itemView.progressBar
    val parent = itemView


    // video player
    val appContext = itemView.context.applicationContext
    var mProgressBar: ProgressBar? = null
    var lastPlayingCover = itemView.rootView.tag as ImageView?

    override fun clear() {

    }


    override fun onBind(position: Int) {
        super.onBind(position)
        parent.tag = this@ItemViewHolder
        textViewTitle.text = InfoList[position].mTitle
        userHandle.text = InfoList[position].mUserHandle
        Glide.with(itemView.context).load(InfoList[position].mCoverUrl).apply(RequestOptions().optionalCenterCrop())
            .into(cover)

        itemView.setOnTouchListener { view, event ->
            Log.d(TAG, "event = $event")
            if (event.action == MotionEvent.ACTION_DOWN) {
                removePreviousPlayView(videoSurfaceView)
                playOnView(position)
                return@setOnTouchListener true
            }

            false
        }

        itemView.setOnClickListener {
            removePreviousPlayView(videoSurfaceView)  // preventing from leakage
        }
    }

    internal fun playOnView(position: Int) {
        // add SurfaceView
        lastPlayingCover?.visibility = View.VISIBLE
        cover.visibility = View.GONE
        lastPlayingCover = cover
        itemView.rootView.tag = lastPlayingCover
        mProgressBar = progressBar
        videoLayout.addView(videoSurfaceView)
        videoSurfaceView.requestFocus()

        // create MediaSource
        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(
            appContext,
            Util.getUserAgent(appContext, appContext.packageName),
            bandwidthMeter
        )
        val uriString = InfoList[position].mUrl
        if (uriString.isNotEmpty()) {
            val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(uriString))

            with(player) {
                prepare(mediaSource)
                playWhenReady = true
            }
        }
    }

    private fun removePreviousPlayView(videoView: PlayerView) {
        val parent = videoView.parent as ViewGroup? ?: return
        val rootView = videoView.rootView as ViewGroup

        val index = parent.indexOfChild(videoView)
        val index2 = rootView.indexOfChild(videoView)
        Log.d(TAG, "removePreviousPlayView(): index = $index, parent = $parent, rootView = $rootView, index2 = $index2")
        if (index >= 0) {
//            parent.removeViewAt(index)
            rootView.removeViewAt(index2)
        }
    }

    companion object {
        const val TAG = "ItemViewHolder"
    }
}
