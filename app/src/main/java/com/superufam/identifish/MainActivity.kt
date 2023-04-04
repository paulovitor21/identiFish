package com.superufam.identifish

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.superufam.identifish.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)

        binding.btnAcessar.setOnClickListener{
            val intent = Intent(this, IdentificacaoActivity::class.java)
            startActivity(intent)
        }

        binding.btnComoUsar.setOnClickListener{
            val intent = Intent(this, InformativoActivity::class.java)
            startActivity(intent)
        }
    }
}