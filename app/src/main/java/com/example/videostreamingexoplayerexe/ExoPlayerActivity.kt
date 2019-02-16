package com.example.videostreamingexoplayerexe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_exo_player.*

class ExoPlayerActivity : AppCompatActivity(), Player.EventListener {
    var player: SimpleExoPlayer? = null

    private var videoUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_exo_player)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        supportActionBar?.hide()

        if (intent.hasExtra(KEY_VIDEO_URI)){
            videoUri = intent.getStringExtra(KEY_VIDEO_URI)
        }

        imageViewExit.setOnClickListener {
            finish()
        }

        setup()
    }

    private fun setup() {
        initializePlayer()
        if (videoUri != null) {
            player?.prepare(buildMediaSource(Uri.parse(videoUri)))
            player?.playWhenReady = true
            player?.addListener(this)
        }
    }

    private fun initializePlayer() {
        if (player == null){
            // 1. create a default TrackSelector
            val secondLoadControl = DefaultLoadControl.Builder()
                .setAllocator(DefaultAllocator(true, 16))
                .setBufferDurationsMs( MIN_BUFFER_DURATION, MAX_BUFFER_DURATION, MIN_PLAYBACK_START_BUFFER, MIN_PLAYBACK_RESUME_BUFFER)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true)
                .createDefaultLoadControl()


            val trackSelectionFactory = AdaptiveTrackSelection.Factory()
            val trackSelector = DefaultTrackSelector(trackSelectionFactory)

            // 2. create the SimpleExoPlayer
            player = ExoPlayerFactory.newSimpleInstance(
               this,
                DefaultRenderersFactory(this),
                trackSelector,
                secondLoadControl
            )

            videoFullScreenPlayer.player = player
        }
    }

    private fun buildMediaSource(uri: Uri): ExtractorMediaSource {
        // Measures bandwidth during playback. Can be null if not required.
        val bandwidthMeter = DefaultBandwidthMeter()
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)
        // This is the MediaSource representing the media to be played.
        val mediaSourceFactory = ExtractorMediaSource.Factory(dataSourceFactory)

        return mediaSourceFactory.createMediaSource(uri)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState){
            Player.STATE_BUFFERING -> {
                spinnerVideoDetails.visibility = View.VISIBLE
            }
            Player.STATE_ENDED -> { }
            Player.STATE_IDLE -> {}
            Player.STATE_READY -> {
                spinnerVideoDetails.visibility = View.GONE
            }
        }
        super.onPlayerStateChanged(playWhenReady, playbackState)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    private fun pausePlayer(){
        player?.playWhenReady = false
        player?.playbackState
    }

    private fun resumePlayer(){
        player?.playWhenReady = true
        player?.playbackState
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()

    }

    override fun onRestart() {
        super.onRestart()
        resumePlayer()
    }

    companion object {
        private const val TAG = "ExoPlayerActivity"

        const val MIN_BUFFER_DURATION = 5000
        const val MAX_BUFFER_DURATION = 15000
        const val MIN_PLAYBACK_START_BUFFER = 1500
        const val MIN_PLAYBACK_RESUME_BUFFER = 5000

        const val DEFAULT_VIDEO_URL = "https://androidwave.com/media/androidwave-video-exo-player.mp4"

        const val KEY_VIDEO_URI = "video_uri"

        fun getStartIntent(context: Context, videoUri: String): Intent =
            Intent(context, ExoPlayerActivity::class.java).apply { putExtra(KEY_VIDEO_URI, videoUri) }


    }
}
