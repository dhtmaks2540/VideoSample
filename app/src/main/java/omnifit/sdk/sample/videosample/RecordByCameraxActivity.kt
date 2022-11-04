package omnifit.sdk.sample.videosample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import omnifit.sdk.sample.videosample.MainActivity.Companion.FILENAME_FORMAT
import omnifit.sdk.sample.videosample.databinding.ActivityRecordByCameraxBinding
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordByCameraxActivity : AppCompatActivity() {
    private val binding: ActivityRecordByCameraxBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_record_by_camerax)
    }
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startCamera()
        captureVideo()

        binding.apply {
            btnEndRecord.setOnClickListener {
                captureVideo()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.pvVideo.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.SD))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                Timber.e("[START_CAMERA] ERROR - $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // 시간제한 33초로 설정해야함 -> Observable 사용
    @SuppressLint("MissingPermission")
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        val curRecording = recording

        // Stop the current recording session
        if (curRecording != null) {
            Timber.d("[CAPTURE_VIDEO] - STOP RECORDING!!")
            curRecording.stop()
            recording = null
            return
        }

        val fileName = "${SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis())}.mp4"
        val cacheFile = File(cacheDir, fileName)
        val fileOutputOptions = FileOutputOptions.Builder(cacheFile)
            .setFileSizeLimit(10000000)
            .build()

        recording = videoCapture.output
            .prepareRecording(this, fileOutputOptions)
            .apply {
                withAudioEnabled()
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {

                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra(CACHE_FILE_EXTRA, cacheFile)
                            startActivity(intent)
                        } else {
                            recording?.close()
                            recording = null
                            Timber.e("Video capture ends with error: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    companion object {
        const val CACHE_FILE_EXTRA = "cache_file"
    }
}