package com.superufam.identifish

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.cert.CertPath
import java.text.DecimalFormat
import java.util.Comparator
import java.util.PriorityQueue

class Classificador(assetManager: AssetManager, modelPath: String, labelPath: String, inputSize: Int) {
    private lateinit var INTERPRETER: Interpreter
    private lateinit var LABEL_LIST: List<String>
    private val INPUT_SIZE: Int = inputSize
    private val PIXEL_SIZE: Int = 3
    private val IMAGE_MEAN = 0
    private val IMAGE_STD = 255.0f
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.4f

    data class Reconhecimento(var id: String = "", var title: String = "", var confidence: Float = 0F) {
        override fun toString(): String {
            return "Título = $title, Confiança = $confidence"
        }
    }

    init {
        INTERPRETER = Interpreter(loadModelFile(assetManager, modelPath))
        LABEL_LIST = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    // carrega uma lista de rótulos (labels) a partir de um arquivo de texto
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    fun reconhecerImagem(bitmap: Bitmap): List<Classificador.Reconhecimento> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val result = Array(1) { FloatArray(LABEL_LIST.size)}
        INTERPRETER.run(byteBuffer, result)

        return getSortedResult(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0

        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val `val` = intValues[pixel++]

                byteBuffer.putFloat((((`val`.shr(16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
                byteBuffer.putFloat((((`val`.shr(8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
                byteBuffer.putFloat((((`val` and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
            }
        }
        return byteBuffer
    }

    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Classificador.Reconhecimento> {
        Log.d("Classificador", "Tamanho da Lista: (%d, %d, %d)".format(labelProbArray.size, labelProbArray[0].size, LABEL_LIST.size))

        val pq = PriorityQueue(
            MAX_RESULTS,
            Comparator<Classificador.Reconhecimento> {
                    (_, _, confidence1), (_, _, confidence2)
                -> java.lang.Float.compare(confidence1, confidence2) * -1
            })

        for (i in LABEL_LIST.indices) {
            val confidence = labelProbArray[0][i]
            if (confidence >= THRESHOLD) {
                pq.add(Classificador.Reconhecimento("" + i,
                    if (LABEL_LIST.size > i)
                        LABEL_LIST[i]
                    else "Desconhecido", confidence*100))
            }
        }
        Log.d("Classificador", "pqsize: (%d".format(pq.size))

        val reconhecimento = ArrayList<Classificador.Reconhecimento>()
        val reconhecimentoSize = Math.min(pq.size, MAX_RESULTS)

        for (i in 0 until reconhecimentoSize) {
            reconhecimento.add(pq.poll())
        }
        return reconhecimento
    }
}