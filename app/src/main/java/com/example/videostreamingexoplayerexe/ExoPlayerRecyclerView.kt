package com.example.videostreamingexoplayerexe

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.videostreamingexoplayerexe.ExoPlayerActivity.Companion.MAX_BUFFER_DURATION
import com.example.videostreamingexoplayerexe.ExoPlayerActivity.Companion.MIN_BUFFER_DURATION
import com.example.videostreamingexoplayerexe.ExoPlayerActivity.Companion.MIN_PLAYBACK_RESUME_BUFFER
import com.example.videostreamingexoplayerexe.ExoPlayerActivity.Companion.MIN_PLAYBACK_START_BUFFER
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
// not useful for Video on RecyclerView
class ExoPlayerRecyclerView : RecyclerView {

    private var videoInfoList: List<VideoInfo> = arrayListOf()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    var player: SimpleExoPlayer? = null
    // surface view for playing video
    var videoSurfaceView: PlayerView? = null
    var mCoverImage: ImageView? = null
    var mProgressBar: ProgressBar? = null

    private lateinit var appContext: Context

    private var playPosition = -1
    private var addedVideo = false
    private var rowParent: View? = null

    constructor(context: Context) : super(context) {
//        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
//        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        initialize(context)
    }

    private fun initialize(context: Context) {
        appContext = context.applicationContext

        // create PlayView
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y


        videoSurfaceView = PlayerView(appContext)
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        // create SimpleExoPlayer
        val bandwidthMeter = DefaultBandwidthMeter()
        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                MIN_BUFFER_DURATION,
                MAX_BUFFER_DURATION,
                MIN_PLAYBACK_START_BUFFER,
                MIN_PLAYBACK_RESUME_BUFFER
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
        videoSurfaceView!!.player = player

        // set Listeners
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    playVideo()
                }
            }
        })

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View) {
                if (addedVideo && rowParent != null && rowParent == view) {
                    removeVideoView(videoSurfaceView!!)
                    playPosition = -1
                    videoSurfaceView!!.visibility = View.INVISIBLE

                }
            }

            override fun onChildViewAttachedToWindow(p0: View) {
            }

        })

        player!!.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        videoSurfaceView!!.alpha = 0.5f
                        Log.d(TAG, "onPlayerStateChanged(): Buffering")
                        mProgressBar?.visibility = View.VISIBLE

                    }

                    Player.STATE_READY -> {
                        Log.d(TAG, "onPlayerStateChanged(): Ready")
                        mProgressBar?.visibility = View.GONE
                        videoSurfaceView?.visibility = View.VISIBLE
                        videoSurfaceView?.alpha = 1.0f
                        mCoverImage?.visibility = View.GONE
                    }

                    Player.STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged(): Ended")
                        player?.seekTo(0)
                    }
                }
            }
        })

    }

    fun onPausePlay() {
        if (videoSurfaceView != null) {
            removeVideoView(videoSurfaceView!!)
            player?.release()
            videoSurfaceView = null
        }
    }

    fun onRestartPlayer() {
        if (videoSurfaceView == null) {
            playPosition = -1
            playVideo()
        }
    }

    fun setVideoInfoList(videoInfoList: List<VideoInfo>) {
        this.videoInfoList = videoInfoList
    }

    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent as ViewGroup? ?: return

        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            addedVideo = false
        }
    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at = playPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(at) ?: return 0

        val location01 = IntArray(2)
        child.getLocationInWindow(location01) // array[0] = x, array[1] = y

        return if (location01[1] < 0) {
            location01[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location01[1]
        }


    }

    fun playVideo() {
        val startPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        var endPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        if (endPosition - startPosition > 1) {
            endPosition = startPosition + 1
        }

        if (startPosition < 0 || endPosition < 0) return

        val targetPosition = if (startPosition != endPosition) {
            val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
            val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
            if (startPositionVideoHeight > endPositionVideoHeight) {
                startPosition
            } else {
                endPosition
            }
        } else {
            startPosition
        }

        if (targetPosition < 0 || targetPosition == playPosition) return

        playPosition = targetPosition
        if (videoSurfaceView == null) return
        videoSurfaceView!!.visibility = View.INVISIBLE
        removeVideoView(videoSurfaceView!!)

        // get target View targetPosition in RecyclerView
        val at = targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(at) ?: return

        val holder = child.tag as VideoRecyclerViewAdapter.ViewHolder?
        if (holder == null) {
            playPosition = -1
            return
        }

        mCoverImage = holder.cover
        val frameLayout = holder.videoLayout
        frameLayout.addView(videoSurfaceView!!)
        addedVideo = true
        rowParent = holder.itemView
        videoSurfaceView?.requestFocus()

        // Bind the player to the view
        videoSurfaceView?.player = player

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory =
            DefaultDataSourceFactory(appContext, Util.getUserAgent(appContext, context.packageName), bandwidthMeter)
        val uriString = videoInfoList[targetPosition].mUrl

        if (uriString.isNotEmpty()) {
            val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(uriString))
            player?.prepare(mediaSource)
            player?.playWhenReady = true

        }

    }

    fun onRelease(){
        player?.release()
        player = null
        rowParent = null
    }

    companion object {
        private const val TAG = "ExoPlayerRecyclerView"
    }
}