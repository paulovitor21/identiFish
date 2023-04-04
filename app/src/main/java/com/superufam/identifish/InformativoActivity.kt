package com.superufam.identifish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.superufam.identifish.databinding.ActivityInformativoBinding

class InformativoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformativoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformativoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define a toolbar como barra de ação
        setSupportActionBar(binding.toolbarIF)

        // Habilita o botão de voltar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Define a ação do botão de voltar
        binding.toolbarIF.setNavigationOnClickListener { onBackPressed() }

        // Define a cor da seta como branca
        val whiteArrow = resources.getDrawable(R.drawable.baseline_arrow_back_24, null)
        supportActionBar?.setHomeAsUpIndicator(whiteArrow)
    }
}