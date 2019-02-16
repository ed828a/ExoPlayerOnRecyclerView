package com.example.videostreamingexoplayerexe

import android.content.Context
import android.net.Uri
import android.support.annotation.IntDef
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.item_empty_layout.view.*
import kotlinx.android.synthetic.main.item_layout.view.*

class VideoRecyclerViewAdapter(val context: Context, val mInfoList: List<VideoInfo>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    // surface view for playing video
    private var videoSurfaceView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private var lastPlayingCover: ImageView? = null
//    private var mProgressBar: ProgressBar? = null
    private val appContext: Context = context.applicationContext


    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        // 1. create SurfaceView
        videoSurfaceView = PlayerView(appContext)
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        videoSurfaceView!!.useController = false
        videoSurfaceView!!.setShowBuffering(SHOW_BUFFERING_ALWAYS)

        // 2. create SimpleExoPlayer
        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                ExoPlayerActivity.MIN_BUFFER_DURATION,
                ExoPlayerActivity.MAX_BUFFER_DURATION,
                ExoPlayerActivity.MIN_PLAYBACK_START_BUFFER,
                ExoPlayerActivity.MIN_PLAYBACK_RESUME_BUFFER
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .createDefaultLoadControl()

        player = ExoPlayerFactory.newSimpleInstance(
            appContext,
            DefaultRenderersFactory(appContext),
            trackSelector,
            loadControl
        )
        // 3. bind SurfaceView to ExoPlayer
        videoSurfaceView?.player = player

        player!!.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        MyCache.transport.lastPlayingCover?.visibility = View.VISIBLE
                        videoSurfaceView!!.alpha = 0.5f
                        Log.d(TAG, "onPlayerStateChanged(): Buffering")

                    }

                    Player.STATE_READY -> {
                        Log.d(TAG, "onPlayerStateChanged(): Ready")
//                        mProgressBar?.visibility = View.GONE
                        MyCache.transport.lastPlayingCover?.visibility = View.GONE
                        videoSurfaceView?.visibility = View.VISIBLE
                        videoSurfaceView?.alpha = 1.0f

                    }

                    Player.STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged(): Ended")
//                        MyCache.transport.lastPlayingCover?.visibility = View.VISIBLE
//                        player?.seekTo(0)
                    }

//                    Player.STATE_IDLE -> {
//                        MyCache.transport.lastPlayingCover?.visibility = View.VISIBLE
//                    }
                }
            }
        })

        MyCache.transport.player = player
        MyCache.transport.videoSurfaceView = videoSurfaceView
    }

    fun onRelease() {
       MyCache.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            VIEW_TYPE_NORMAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
                return ItemViewHolder(view,  mInfoList)
            }

            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty_layout, parent, false)
                return EmptyViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
                return ItemViewHolder(view,  mInfoList)
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

        private const val TAG = "VideoRecyclerViewAdapter"

        @IntDef(VIEW_TYPE_EMPTY, VIEW_TYPE_NORMAL)
        @Retention(AnnotationRetention.SOURCE) // not store in Binary code
        annotation class ViewType

        const val VIEW_TYPE_EMPTY = 0
        const val VIEW_TYPE_NORMAL = 1


    }
}