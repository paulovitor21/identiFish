package com.superufam.identifish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import com.superufam.identifish.databinding.ActivitySearchResultBinding

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_search_result)
        setContentView(binding.root)

        val resultadoPesquisa = intent.getStringExtra("resultado_pesquisa")
        val webView = findViewById<WebView>(R.id.web_view)
        webView.clearCache(true)
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://pt.wikipedia.org/wiki/Especial:Pesquisar?search=$resultadoPesquisa")

        // Define a toolbar como barra de ação
        setSupportActionBar(binding.toolbar)

        // Habilita o botão de voltar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Define a ação do botão de voltar
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Define a cor da seta como branca
        val whiteArrow = resources.getDrawable(R.drawable.baseline_arrow_back_24, null)
        supportActionBar?.setHomeAsUpIndicator(whiteArrow)

    }
}

private fun Toolbar.setNavigationOnClickListener(onBackPressed: Unit) {

}
