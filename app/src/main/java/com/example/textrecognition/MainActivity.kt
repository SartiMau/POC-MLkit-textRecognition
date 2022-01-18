package com.example.textrecognition

import android.Manifest.permission.CAMERA
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.textrecognition.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imagePreview: PreviewView
    private lateinit var startScanButton: Button
    private lateinit var camera: Camera
    private lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePreview = binding.viewFinder
        startScanButton = binding.startScanButton

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding.startScanButton.setOnClickListener { startScan() }
    }


    private fun startScan() {
        val bitmap = imagePreview.bitmap

        if (bitmap != null){
            filePath = tempFileImage(bitmap, "name")
            //byteArray = convertBitmapToByteArray(bitmap)
        }

        startActivity(DetectionReviewActivity.newIntent(this, filePath))
    }

    private fun tempFileImage(bitmap: Bitmap, takenImg: String): String {
        val outputDir: File = this.getCacheDir()
        val imageFile = File(outputDir, "$takenImg.jpg")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: java.lang.Exception) {
            Log.e("error", "Error writing file", e)
        }
        return imageFile.getAbsolutePath()
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray  {
        val buffer = ByteArrayOutputStream(bitmap.width /3 * bitmap.height /3)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer)
        return buffer.toByteArray()
    }

    /*
    val file =  File(filesDir, "picFromCamera")
    uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
    if (allPermissionsGranted()) {
        resultLauncher.launch(uri)
    } else {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }
}
    */

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()


                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

                preview.setSurfaceProvider(imagePreview.surfaceProvider)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //NOTHING
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}
