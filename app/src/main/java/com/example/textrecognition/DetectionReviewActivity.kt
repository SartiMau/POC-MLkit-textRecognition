package com.example.textrecognition

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.textrecognition.databinding.DetectionReviewBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class DetectionReviewActivity : AppCompatActivity() {

    private lateinit var binding: DetectionReviewBinding
    private lateinit var filePath: String
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetectionReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = intent.getSerializableExtra("filePath") as String

        analizeText()
    }

    private fun analizeText() {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val file = File(filePath)
        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        val image = InputImage.fromBitmap(bitmap, 0)

        binding.imageContainer.setImageBitmap(bitmap)

        recognizer.process(image).addOnSuccessListener {
            val resultText = it.text

            binding.recognizedText.text = resultText
        }
    }

    fun restartScanning(view: View) {
        onBackPressed()
    }

    companion object {
        fun newIntent(context: Context, filePath: String): Intent =
            Intent(context, DetectionReviewActivity::class.java).apply { putExtra("filePath", filePath) }
    }
}
