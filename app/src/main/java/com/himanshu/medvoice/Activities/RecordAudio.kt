package com.himanshu.medvoice.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.himanshu.medvoice.databinding.ActivityRecordAudioBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAudio : AppCompatActivity() {

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var binding: ActivityRecordAudioBinding
    private lateinit var recordedFile: File

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

        recordedFile = getOutputMediaFile()
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
        mediaRecorder.setOutputFile(recordedFile?.absolutePath)

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


        // Check if the file is not null and exists
        if (recordedFile != null && recordedFile.exists()) {
            // Perform the file upload here
            uploadAudioFile(recordedFile)
        }
    }

    private fun uploadAudioFile(audioFile: File) {
        val client = OkHttpClient()

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", audioFile.name, audioFile.asRequestBody("audio/3gp".toMediaTypeOrNull()))
            .build()

        val request: Request = Request.Builder()
            .url("http://10.0.2.2:5000/process_audio")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error uploading audio file", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    // Process the response data as needed
                    Log.d("HJU",responseData.toString())
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Audio file uploaded successfully", Toast.LENGTH_SHORT).show()
                        binding.response.text = responseData.toString()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error uploading audio file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getOutputMediaFile(): File {
        // Get the Download directory
        val mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

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
