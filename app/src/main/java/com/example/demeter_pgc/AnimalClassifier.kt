package com.example.demeter_pgc

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer

/**
 * Motor de clasificación de animales usando YOLOv8-cls + TensorFlow Lite
 * Clases: caballo, gallina, gato, oveja, perro, vaca
 */
class AnimalClassifier(private val context: Context) {

    companion object {
        const val MODEL_FILE          = "best_animales.tflite"
        const val LABELS_FILE         = "labels.txt"
        const val IMG_SIZE            = 224
        const val MAX_RESULTS         = 3
        const val CONFIDENCE_THRESHOLD = 0.4f
        const val MEAN                = 0.0f
        const val STD                 = 255.0f
    }

    data class Recognition(
        val label: String,
        val confidence: Float
    ) {
        val confidencePercent: String get() = "%.1f%%".format(confidence * 100)
    }

    private val interpreter: Interpreter
    private val labels: List<String>
    private val imageProcessor: ImageProcessor
    private val outputBuffer: TensorBuffer

    init {
        val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
        val options = Interpreter.Options().apply {
            numThreads = 4
            useNNAPI   = true
        }
        interpreter = Interpreter(modelBuffer, options)
        labels      = FileUtil.loadLabels(context, LABELS_FILE)

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(MEAN, STD))
            .build()

        val outputShape    = interpreter.getOutputTensor(0).shape()
        val outputDataType = interpreter.getOutputTensor(0).dataType()
        outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)
    }

    fun classify(bitmap: Bitmap): List<Recognition> {
        val tensorImage    = TensorImage.fromBitmap(bitmap)
        val processedImage = imageProcessor.process(tensorImage)
        interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())

        return TensorLabel(labels, outputBuffer)
            .mapWithFloatValue
            .entries
            .filter  { it.value >= CONFIDENCE_THRESHOLD }
            .sortedByDescending { it.value }
            .take(MAX_RESULTS)
            .map { Recognition(label = it.key, confidence = it.value) }
    }

    fun close() = interpreter.close()
}
