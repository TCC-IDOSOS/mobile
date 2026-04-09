package com.example.fisioaging.ui.utt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R

class UttDetalhesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_detalhes)
        supportActionBar?.title = "Detalhes UTT"

        val videoView = findViewById<VideoView>(R.id.videoView2)

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.utt}")

        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
        }

        videoView.setOnCompletionListener {
            videoView.postDelayed({
                videoView.start()
            }, 1500)
        }
        videoView.start()

        findViewById<Button>(R.id.btn_iniciar_utt).setOnClickListener {
            startActivity(Intent(this, UttExecucaoActivity::class.java))
            finish()
        }
    }
}