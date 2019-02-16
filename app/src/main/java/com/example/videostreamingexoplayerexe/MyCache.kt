package com.example.videostreamingexoplayerexe


object MyCache {
    val transport = Transporter()

    fun reset(){
        with(transport){
            player?.release()
            player = null
            videoSurfaceView = null
            lastPlayingCover = null
        }
    }
}