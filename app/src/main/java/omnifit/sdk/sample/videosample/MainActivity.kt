package omnifit.sdk.sample.videosample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import omnifit.sdk.sample.videosample.RecordByCameraxActivity.Companion.CACHE_FILE_EXTRA
import omnifit.sdk.sample.videosample.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            btnUpload.setOnClickListener {
                pickVideoFromGallery()
            }

            btnRecord.setOnClickListener {
                if (allPermissionCheck()) {
                    startActivity(Intent(this@MainActivity, RecordActivity::class.java))
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, REQUEST_PERMISSION_CODE)
                }
            }

            btnRecordUsingCamerax.setOnClickListener {
                if (allPermissionCheck()) {
                    startActivity(Intent(this@MainActivity, RecordByCameraxActivity::class.java))
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, REQUEST_PERMISSION_CODE_X)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val file = intent.getSerializableExtra(CACHE_FILE_EXTRA) as? File
        Timber.d("[ON_START] - file : $file")
        file?.delete()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startActivity(Intent(this, RecordActivity::class.java))
                } else {
                    showMessage("[PERMISSION]\nPERMISSION DENIED")
                    Timber.d("[PERMISSION] - PERMISSION DENIED")
                }
            }
            REQUEST_PERMISSION_CODE_X -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startActivity(Intent(this@MainActivity, RecordByCameraxActivity::class.java))
                } else {
                    showMessage("[PERMISSION]\nPERMISSION DENIED")
                    Timber.d("[PERMISSION] - PERMISSION DENIED")
                }
            }
            else -> {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_VIDEO -> {
                when (resultCode) {
                    RESULT_OK -> {
                        if (data == null) {
                            showMessage("NO DATA!!")
                        } else {
                            val selectedVideoUri = data.data
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(applicationContext, selectedVideoUri)
                            // 메타데이터 획득 후 필터링(용량, 총 프레임 수)
                            Timber.d("[VIDEO INFORMATION] - ${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)}\n" +
                                    "${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)}\n" +
                                    "${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)}\n" +
                                    "${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)}")
                        }
                    }
                    RESULT_CANCELED -> {
                        showMessage("CANCEL PICK VIDEO")
                    }
                }
            }
        }
    }

    private fun allPermissionCheck() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        val mimeType = arrayOf("video/mp4", "video/mov")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        startActivityForResult(intent, PICK_VIDEO)
    }

    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_PERMISSION_CODE = 100
        private const val REQUEST_PERMISSION_CODE_X = 101
        private const val PICK_VIDEO = 1000
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}