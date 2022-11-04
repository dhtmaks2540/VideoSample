package omnifit.sdk.sample.videosample

import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import omnifit.sdk.sample.videosample.databinding.ActivityRecordBinding
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecordActivity : AppCompatActivity() {
    private var recorder: MediaRecorder? = null
    private val binding: ActivityRecordBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_record)
    }
    private val fileName: String by lazy {
        "${SimpleDateFormat(MainActivity.FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis())}.mp4"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            btnEndRecord.setOnClickListener {
                stopRecording()
            }

            sfvVideo.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    startRecording()
                }

                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                }

                override fun surfaceDestroyed(p0: SurfaceHolder) {
                }
            })
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()

            val intent = Intent(this@RecordActivity, MainActivity::class.java)
            intent.putExtra(RecordByCameraxActivity.CACHE_FILE_EXTRA, File("${cacheDir}/$fileName"))
            startActivity(intent)
        }
        recorder = null
    }

    private fun startRecording() {
        val frameRate = 30
        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile("${cacheDir}/$fileName")
                setPreviewDisplay(binding.sfvVideo.holder.surface)
                setMaxFileSize(10000000) // Max File 10MB
                setMaxDuration((1000 / frameRate) * 1000)
                setOrientationHint(90) // 90도 회전
                setVideoFrameRate(frameRate) // Frame rate
                setVideoEncodingBitRate(3449000)
                setVideoSize(720, 480) // Video size
                prepare()
                start()
            }
        } catch (e: IOException) {
            Timber.e("[START_RECORDING] - ERROR : ${e.message}")
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
}