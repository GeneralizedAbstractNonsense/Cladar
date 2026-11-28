package com.example.cladar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback == null) return@registerForActivityResult
        val results = if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        } else null
        filePathCallback?.onReceiveValue(results)
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true

            // Collega l'interfaccia nativa Android al JavaScript della pagina
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidBridge")

            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                // Abilita la selezione dei file per caricamento foto e import JSON
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                    this@MainActivity.filePathCallback = filePathCallback

                    val intent = fileChooserParams?.createIntent()
                    try {
                        fileChooserLauncher.launch(intent)
                    } catch (e: Exception) {
                        this@MainActivity.filePathCallback = null
                        return false
                    }
                    return true
                }
            }
        }

        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    
    class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun savePdf(base64Data: String, filename: String) {
            try {
                val safeFilename = filename.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    .ifBlank { "cladar_export.pdf" }
                    .let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, safeFilename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri == null) throw Exception("Unable to create the PDF file")
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(bytes)
                        stream.flush()
                    } ?: throw Exception("Unable to open the PDF file")
                    showToast("PDF saved in Download: $safeFilename")
                } else {
                    @Suppress("DEPRECATION")
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) downloadsDir.mkdirs()
                    val file = File(downloadsDir, safeFilename)
                    FileOutputStream(file).use { stream ->
                        stream.write(bytes)
                        stream.flush()
                    }
                    showToast("PDF saved in Download: $safeFilename")
                }
            } catch (e: Exception) {
                showToast("Error in saving PDF: ${e.message}")
            }
        }

        @JavascriptInterface
        fun saveJson(jsonString: String, filename: String) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { stream ->
                            stream.write(jsonString.toByteArray())
                        }
                        showToast("Backup saved in Download: $filename")
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, filename)
                    FileOutputStream(file).use { stream ->
                        stream.write(jsonString.toByteArray())
                    }
                    showToast("Backup saved in Download: $filename")
                }
            } catch (e: Exception) {
                showToast("Error in saving: ${e.message}")
            }
        }

        private fun showToast(message: String) {
            (context as Activity).runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
