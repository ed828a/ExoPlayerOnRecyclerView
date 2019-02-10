package com.example.videostreamingexoplayerexe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout.VERTICAL
import kotlinx.android.synthetic.main.activity_recycler.*

class RecyclerActivity : AppCompatActivity() {

    private var firstTime = true
    private val videoList = prepareVideoList()
    private var mAdapter: VideoRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)


        val dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_drawable)!!
        with(recyclerViewFeed){

            layoutManager = LinearLayoutManager(this@RecyclerActivity, VERTICAL, false)
            addItemDecoration(DividerItemDecoration(dividerDrawable))
            itemAnimator = DefaultItemAnimator()
            mAdapter = VideoRecyclerViewAdapter(this@RecyclerActivity, videoList)
            adapter = mAdapter
        }

//        if (firstTime){
//            Handler(Looper.getMainLooper()).post {
//                recyclerViewFeed.playVideo()
//            }
//            firstTime = false
//        }

        recyclerViewFeed.scrollToPosition(0)

    }

    override fun onDestroy() {
        super.onDestroy()

        mAdapter?.onRelease()
    }

    private fun prepareVideoList(): List<VideoInfo> {

        val videoInfoList: List<VideoInfo> = listOf(
            VideoInfo(
                1,
                "Do you think the concept of marriage will no longer exist in the future?",
                "https://androidwave.com/media/androidwave-video-1.mp4",
                "https://androidwave.com/media/images/exo-player-in-recyclerview-in-android-1.png",
                "@h.pandya"
            ),

            VideoInfo(
                2,
                "If my future husband doesn't cook food as good as my mother should I scold him?",
                "https://androidwave.com/media/androidwave-video-2.mp4",
                "https://androidwave.com/media/images/exo-player-in-recyclerview-in-android-2.png",
                "@hardik.patel"
            ),

            VideoInfo(
                3,
                "Give your opinion about the Ayodhya temple controversy.",
                "https://androidwave.com/media/androidwave-video-3.mp4",
                "https://androidwave.com/media/images/exo-player-in-recyclerview-in-android-3.png",
                "@arun.gandhi"
            ),

            VideoInfo(
                4,
                "When did kama founders find sex offensive to Indian traditions",
                "https://androidwave.com/media/androidwave-video-6.mp4",
                "https://androidwave.com/media/images/exo-player-in-recyclerview-in-android-4.png",
                "@sachin.patel"
            ),

            VideoInfo(
                5,
                "When did you last cry in front of someone?",
                "https://androidwave.com/media/androidwave-video-5.mp4",
                "https://androidwave.com/media/images/exo-player-in-recyclerview-in-android-5.png",
                "@monika.sharma"
            )
        )

        val result = arrayListOf<VideoInfo>()
        result.addAll(videoInfoList)
        result.addAll(videoInfoList)
        return result
    }

}
