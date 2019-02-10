package com.example.videostreamingexoplayerexe

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import com.example.videostreamingexoplayerexe.ExoPlayerActivity.Companion.DEFAULT_VIDEO_URL
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_prompts.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        buttonPlayDefaultVideo.setOnClickListener {
            startActivity(ExoPlayerActivity.getStartIntent(it.context, DEFAULT_VIDEO_URL))
        }

        buttonPlayUrlVideo.setOnClickListener {
            showDialogPrompt(it.context)
        }
    }

    private fun showDialogPrompt(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_prompts, null)


        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("OK"){dialog, which ->
                val isURL = Patterns.WEB_URL.matcher(view.editTextDialogUrlInput.text.toString().trim()).matches()
                if (isURL){
                    val intent = ExoPlayerActivity.getStartIntent(this, DEFAULT_VIDEO_URL)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, getString(R.string.error_message_url_not_valid), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel"){dialog, which ->
               dialog.cancel()
            }
            .create()
            .show()
    }
}
