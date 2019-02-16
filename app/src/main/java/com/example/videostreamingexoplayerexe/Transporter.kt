package com.example.videostreamingexoplayerexe

import android.widget.ImageView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

data class Transporter(
    var videoSurfaceView: PlayerView? = null,
    var player: SimpleExoPlayer? = null,
    var lastPlayingCover: ImageView? = null
)