package com.superufam.identifish

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.Matrix
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.superufam.identifish.databinding.ActivityIdentificacaoBinding
import java.io.IOException
import java.text.DecimalFormat

class IdentificacaoActivity : AppCompatActivity() {

    private var mCamera: Camera? = null

    private lateinit var binding: ActivityIdentificacaoBinding

    private lateinit var mClassificador: Classificador
    private lateinit var mBitmap: Bitmap

    private val mCameraRequestCode = 0
    private val mGaleriaRequestCode = 2

    private val mInputSize = 224
    private val mModelPath = "model_fish_detection.tflite"
    private val mLabelPath = "labels_fish.txt"
    private val mSamplePath = "fish_placeholder.png"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentificacaoBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)

        mClassificador = Classificador(assets, mModelPath, mLabelPath, mInputSize)

        resources.assets.open(mSamplePath).use {
            mBitmap = BitmapFactory.decodeStream(it)
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
            binding.phtoImageView.setImageBitmap(mBitmap)
        }

        binding.btnCamera.setOnClickListener{
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, mCameraRequestCode)
        }

        binding.btnImportar.setOnClickListener{
            val callImportIntent = Intent(Intent.ACTION_PICK)
            startActivityForResult(callImportIntent, mGaleriaRequestCode)
        }

        binding.btnProcessar.isEnabled = false

        binding.btnProcessar.setOnClickListener{
            val results = mClassificador.reconhecerImagem(mBitmap).firstOrNull()
            val decimalFormat = DecimalFormat("#.##")
            val confidenceFormatted = decimalFormat.format(results?.confidence)
            binding.ResultadoTextView.text = results?.title + "\n Confiança: " + confidenceFormatted + "%"
            binding.ResultadoTextView.visibility = View.VISIBLE // torna o TextView visível

            binding.btnDetalhes.visibility = View.VISIBLE
        }

        binding.btnDetalhes.setOnClickListener{
            val results = mClassificador.reconhecerImagem(mBitmap).firstOrNull()
            val titulo = results?.title

            val intent = Intent(this, SearchResultActivity::class.java)
            intent.putExtra("resultado_pesquisa", titulo)
            startActivity(intent)
        }

        // Define a toolbar como barra de ação
        setSupportActionBar(binding.toolbarID)

        // Habilita o botão de voltar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Define a ação do botão de voltar
        binding.toolbarID.setNavigationOnClickListener { onBackPressed() }

        // Define a cor da seta como branca
        val whiteArrow = resources.getDrawable(R.drawable.baseline_arrow_back_24, null)
        supportActionBar?.setHomeAsUpIndicator(whiteArrow)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCameraRequestCode) {
            // Considere o caso da câmera anulada
            if (resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = data.extras!!.get("data") as Bitmap
                mBitmap = scaleImage(mBitmap)
                val toast = Toast.makeText(this, ("Corte de imagem para: w = ${mBitmap.width} h = ${mBitmap.height}"), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                //toast.show()
                binding.phtoImageView.setImageBitmap(mBitmap)
                binding.btnProcessar.isEnabled = true // Habilita o botão "Processar"
                //ResultTextView.txt = "Sua imagem fotográfica definida agora."
            } else {
                Toast.makeText(this, "Câmera cancelada..", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == mGaleriaRequestCode) {
            if (data != null) {
                val uri = data.data

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                println("Sucesso!")
                mBitmap = scaleImage(mBitmap)
                binding.phtoImageView.setImageBitmap(mBitmap)
                binding.btnProcessar.isEnabled = true // Habilitar o botão "Processar"
            }
        } else {
            Toast.makeText(this, "Código de solicitação não reconhecido", Toast.LENGTH_LONG).show()
        }

    }
    // redimensiona uma imagem
    private fun scaleImage(bitmap: Bitmap?): Bitmap {
        val originalWith = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWith = mInputSize.toFloat() / originalWith
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWith, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, originalWith, originalHeight, matrix, true)

    }

}