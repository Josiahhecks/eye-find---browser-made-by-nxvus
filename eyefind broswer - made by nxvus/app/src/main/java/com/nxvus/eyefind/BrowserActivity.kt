package com.nxvus.eyefind

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

class BrowserActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var backButton: View
    private lateinit var homeButton: View
    private lateinit var newIdentityButton: View
    private lateinit var statusIndicator: TextView
    private lateinit var torManager: TorManager
    private lateinit var initialUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        torManager = TorManager(this)

        initialUrl = intent.getStringExtra("url") ?: "https://check.torproject.org"
        val title = intent.getStringExtra("title") ?: "Eyefind"

        setupViews(title)
        setupWebView()
        setupTorGate()
    }

    private fun setupViews(title: String) {
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)
        backButton = findViewById(R.id.backButton)
        homeButton = findViewById(R.id.homeButton)
        newIdentityButton = findViewById(R.id.newIdentityButton)
        statusIndicator = findViewById(R.id.statusIndicator)

        titleText.text = title

        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }

        homeButton.setOnClickListener {
            finish()
        }

        newIdentityButton.setOnClickListener {
            torManager.requestNewIdentity()
            Toast.makeText(this, "New Tor identity requested", Toast.LENGTH_SHORT).show()
            webView.reload()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = false
            databaseEnabled = false
            setGeolocationEnabled(false)
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = "$userAgentString EyefindTor"
        }

        webView.webViewClient = TorWebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }

        disableWebRTCLeak()
    }

    private fun setupTorGate() {
        torManager.status.observe(this) { status ->
            updateStatus(status)
            when (status) {
                TorManager.TorStatus.ACTIVE -> {
                    torManager.configureWebViewProxy {
                        lifecycleScope.launch {
                            if (torManager.testTorConnection()) {
                                if (webView.url == null || webView.url == "about:blank") {
                                    loadUrl(initialUrl)
                                }
                            } else {
                                showBlockedPage("Tor proxy did not pass the Tor check.")
                            }
                        }
                    }
                }
                TorManager.TorStatus.NOT_INSTALLED -> showBlockedPage("Orbot is not installed. Install and start Orbot to browse.")
                TorManager.TorStatus.INSTALLED_NOT_RUNNING -> {
                    showBlockedPage("Orbot is installed but Tor is not running.")
                    torManager.startOrbot()
                }
                TorManager.TorStatus.ERROR -> showBlockedPage("Tor is unavailable.")
                else -> showBlockedPage("Waiting for Tor before loading the network.")
            }
        }
        torManager.checkTorStatus()
    }

    private fun updateStatus(status: TorManager.TorStatus) {
        val (text, colorRes) = when (status) {
            TorManager.TorStatus.ACTIVE -> "Tor Active" to R.color.tor_active
            TorManager.TorStatus.STARTING -> "Tor Starting" to R.color.tor_starting
            TorManager.TorStatus.CHECKING -> "Checking Tor" to R.color.tor_starting
            TorManager.TorStatus.ERROR -> "Tor Unavailable" to R.color.tor_inactive
            TorManager.TorStatus.NOT_INSTALLED -> "Orbot Missing" to R.color.tor_inactive
            TorManager.TorStatus.INSTALLED_NOT_RUNNING -> "Tor Unavailable" to R.color.tor_inactive
            TorManager.TorStatus.UNKNOWN -> "Tor Unknown" to R.color.tor_inactive
        }
        statusIndicator.text = text
        statusIndicator.setTextColor(getColor(colorRes))
    }

    @SuppressLint("JavascriptInterface")
    private fun disableWebRTCLeak() {
        webView.evaluateJavascript(
            """
            (function() {
                navigator.mediaDevices = navigator.mediaDevices || {};
                navigator.mediaDevices.getUserMedia = function() {
                    return Promise.reject(new Error('getUserMedia disabled for privacy'));
                };
                if (window.RTCPeerConnection) {
                    window.RTCPeerConnection = function() {
                        throw new Error('WebRTC disabled for privacy');
                    };
                }
            })();
            """.trimIndent(), null
        )
    }

    private fun loadUrl(url: String) {
        val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
        webView.loadUrl(finalUrl)
    }

    private fun showBlockedPage(reason: String) {
        val page = """
            <html><body style="font-family:sans-serif;background:#f5f5f5;color:#1a1a1a;padding:18px">
            <h3>Eyefind Tor Browser</h3>
            <p>Network access is blocked until Tor is active.</p>
            <p>$reason</p>
            </body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL("about:blank", page, "text/html", "UTF-8", null)
    }

    private fun blockedResponse(reason: String): WebResourceResponse {
        val body = "Blocked by Eyefind Tor fail-closed policy: $reason"
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            503,
            "Tor Unavailable",
            mapOf("Cache-Control" to "no-store"),
            ByteArrayInputStream(body.toByteArray())
        )
    }

    inner class TorWebViewClient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return blockedResponse("invalid request")
            if (torManager.status.value != TorManager.TorStatus.ACTIVE) {
                return blockedResponse("Tor is not active")
            }
            if (request.method != "GET") {
                return blockedResponse("request method cannot be safely proxied")
            }

            return try {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8118))
                val connection = URL(url).openConnection(proxy) as HttpURLConnection

                connection.apply {
                    connectTimeout = 30000
                    readTimeout = 30000
                    requestMethod = request.method
                    instanceFollowRedirects = true
                }

                request.requestHeaders.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                connection.connect()

                val contentType = connection.contentType ?: "text/html"
                val contentEncoding = connection.contentEncoding
                val statusCode = connection.responseCode
                val responseMessage = connection.responseMessage

                val inputStream: InputStream = if (statusCode >= 200 && statusCode < 300) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: return blockedResponse("empty proxy response")
                }

                WebResourceResponse(
                    contentType,
                    contentEncoding,
                    statusCode,
                    responseMessage,
                    connection.headerFields
                        .filterKeys { it != null }
                        .mapKeys { it.key.orEmpty() }
                        .mapValues { it.value.joinToString(", ") },
                    inputStream
                )
            } catch (e: Exception) {
                blockedResponse("proxy connection failed")
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progressBar.visibility = View.GONE
            disableWebRTCLeak()
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            Toast.makeText(this@BrowserActivity, "Error: $description", Toast.LENGTH_SHORT).show()
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.cancel()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearCache(true)
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
        torManager.unregisterReceiver()
    }
}
