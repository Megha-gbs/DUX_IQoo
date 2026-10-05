package com.waycheck.sensors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraAnalyzer(
    private val context: Context
) : ImageAnalysis.Analyzer {

    private val _detections = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val detections: StateFlow<List<Pair<String, Float>>> = _detections.asStateFlow()

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private var imageClassifier: ImageClassifier? = null
    private var lastInferenceTimeMs = 0L
    private val inferenceIntervalMs = 250L // ~4 FPS inference for smooth performance

    init {
        initClassifier()
    }

    private fun initClassifier() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("classifier.tflite")
                .build()

            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(5)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            imageClassifier = ImageClassifier.createFromOptions(context, options)
            _isModelLoaded.value = true
            Log.d("CameraAnalyzer", "MediaPipe ImageClassifier initialized successfully")
        } catch (e: Exception) {
            Log.w("CameraAnalyzer", "Failed to load MediaPipe model with classifier.tflite: ${e.message}")
            try {
                val fallbackOptions = BaseOptions.builder()
                    .setModelAssetPath("classifier.task")
                    .build()
                val options = ImageClassifier.ImageClassifierOptions.builder()
                    .setBaseOptions(fallbackOptions)
                    .setMaxResults(5)
                    .setRunningMode(RunningMode.IMAGE)
                    .build()
                imageClassifier = ImageClassifier.createFromOptions(context, options)
                _isModelLoaded.value = true
                Log.d("CameraAnalyzer", "MediaPipe ImageClassifier loaded via fallback classifier.task")
            } catch (e2: Exception) {
                Log.e("CameraAnalyzer", "MediaPipe fallback also failed: ${e2.message}")
                _isModelLoaded.value = false
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastInferenceTimeMs < inferenceIntervalMs) {
            imageProxy.close()
            return
        }
        lastInferenceTimeMs = now

        val classifier = imageClassifier
        if (classifier == null) {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxy.toBitmap()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            val result = classifier.classify(mpImage)

            val parsedResults = mutableListOf<Pair<String, Float>>()
            result.classificationResult()
                .classifications()
                .firstOrNull()
                ?.categories()
                ?.forEach { category ->
                    val categoryName = category.categoryName() ?: ""
                    val score = category.score()
                    if (categoryName.isNotBlank() && score >= 0.10f) {
                        parsedResults.add(Pair(categoryName, score))
                    }
                }

            _detections.value = parsedResults

        } catch (e: Exception) {
            Log.e("CameraAnalyzer", "Error analyzing frame: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        try {
            imageClassifier?.close()
        } catch (e: Exception) {
            // Ignore
        }
        imageClassifier = null
    }
}
