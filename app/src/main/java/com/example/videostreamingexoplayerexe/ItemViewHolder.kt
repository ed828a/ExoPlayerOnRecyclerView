package com.example.videostreamingexoplayerexe

import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
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
                val videoSurfaceView = MyCache.transport.videoSurfaceView!!
                removePreviousPlayView(videoSurfaceView)
                playOnView(position)
                return@setOnTouchListener false
            }

            false
        }

        itemView.setOnClickListener {
            val videoSurfaceView = MyCache.transport.videoSurfaceView!!
            removePreviousPlayView(videoSurfaceView)  // preventing from leakage
            val intent = Intent(this.appContext, ExoPlayerActivity::class.java)
            intent.putExtra(ExoPlayerActivity.KEY_VIDEO_URI, InfoList[position].mUrl)
            it.context.startActivity(intent)
        }
    }

    internal fun playOnView(position: Int) {
        // add SurfaceView
        val lastPlayingCover = MyCache.transport.lastPlayingCover
        lastPlayingCover?.visibility = View.VISIBLE
        cover.visibility = View.GONE
        MyCache.transport.lastPlayingCover = cover
        itemView.rootView.tag = lastPlayingCover
        mProgressBar = progressBar
        val videoSurfaceView = MyCache.transport.videoSurfaceView
        videoLayout.addView(videoSurfaceView)
        videoSurfaceView?.requestFocus()

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
            val player = MyCache.transport.player!!
            with(player) {
                prepare(mediaSource)
                playWhenReady = true
            }
        }
    }

    private fun removePreviousPlayView(videoView: PlayerView) {
        val parent = videoView.parent as ViewGroup? ?: return

        val index = parent.indexOfChild(videoView)
        Log.d(TAG, "removePreviousPlayView(): index = $index, parent = $parent")
        if (index >= 0) {
            parent.removeViewAt(index)
        }
//        MyCache.transport.lastPlayingCover?.visibility = View.VISIBLE
    }

    companion object {
        const val TAG = "ItemViewHolder"
    }
}
