package com.himanshu.medvoice.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.himanshu.medvoice.databinding.ActivityRecordAudioBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAudio : AppCompatActivity() {

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var binding: ActivityRecordAudioBinding

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request permission to record audio if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }

        binding.recordBtn.setOnClickListener {
            onRecordButtonClicked()
        }
    }

    private fun onRecordButtonClicked() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        Toast.makeText(applicationContext,"Start",Toast.LENGTH_SHORT).show()
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setOutputFile(getOutputMediaFile()?.absolutePath)

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
            binding.recordBtn.text = "Stop Recording"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        Toast.makeText(applicationContext,"Stop",Toast.LENGTH_SHORT).show()

        mediaRecorder.stop()
        mediaRecorder.release()
        isRecording = false
        binding.recordBtn.text = "Start Recording"
    }

    private fun getOutputMediaFile(): File? {
        // Get the Download directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create a directory in the Download folder if it doesn't exist
        val mediaStorageDir = File(downloadsDir, "YourApp/Audio")

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }

        // Create a file with a unique name (you may want to add a timestamp)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "audio_record_$timestamp.3gp"
        return File(mediaStorageDir, fileName)
    }


    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}
