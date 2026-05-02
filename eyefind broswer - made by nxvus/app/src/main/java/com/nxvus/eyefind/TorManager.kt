package com.nxvus.eyefind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.URL

class TorManager(private val context: Context) {

    companion object {
        const val ORBOT_PACKAGE = "org.torproject.android"
        const val ORBOT_HTTP_PROXY_HOST = "127.0.0.1"
        const val ORBOT_HTTP_PROXY_PORT = 8118
        const val ORBOT_SOCKS_PROXY_HOST = "127.0.0.1"
        const val ORBOT_SOCKS_PROXY_PORT = 9050

        const val ACTION_START = "org.torproject.android.intent.action.START"
        const val ACTION_STOP = "org.torproject.android.intent.action.STOP"
        const val ACTION_NEWNYM = "org.torproject.android.intent.action.NEWNYM"
        const val ACTION_STATUS = "org.torproject.android.intent.action.STATUS"
        const val EXTRA_STATUS = "org.torproject.android.intent.extra.STATUS"
        const val EXTRA_PACKAGE_NAME = "org.torproject.android.intent.extra.PACKAGE_NAME"

        const val STATUS_ON = "ON"
        const val STATUS_OFF = "OFF"
        const val STATUS_STARTING = "STARTING"
        const val STATUS_STOPPING = "STOPPING"
    }

    enum class TorStatus {
        UNKNOWN,
        CHECKING,
        NOT_INSTALLED,
        INSTALLED_NOT_RUNNING,
        STARTING,
        ACTIVE,
        ERROR
    }

    private val _status = MutableLiveData<TorStatus>(TorStatus.UNKNOWN)
    val status: LiveData<TorStatus> = _status

    private val _bootstrapProgress = MutableLiveData<Int>(0)
    val bootstrapProgress: LiveData<Int> = _bootstrapProgress

    private var statusReceiver: BroadcastReceiver? = null

    fun checkOrbotInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun startOrbot() {
        if (!checkOrbotInstalled()) {
            _status.value = TorStatus.NOT_INSTALLED
            return
        }

        _status.value = TorStatus.STARTING

        val intent = Intent(ACTION_START).apply {
            `package` = ORBOT_PACKAGE
            putExtra(EXTRA_PACKAGE_NAME, context.packageName)
        }
        context.sendBroadcast(intent)

        registerStatusReceiver()
    }

    fun stopOrbot() {
        val intent = Intent(ACTION_STOP).apply {
            `package` = ORBOT_PACKAGE
        }
        context.sendBroadcast(intent)
    }

    fun requestNewIdentity() {
        val intent = Intent(ACTION_NEWNYM).apply {
            `package` = ORBOT_PACKAGE
        }
        context.sendBroadcast(intent)
    }

    fun checkTorStatus() {
        if (!checkOrbotInstalled()) {
            _status.value = TorStatus.NOT_INSTALLED
            return
        }

        _status.value = TorStatus.CHECKING

        val intent = Intent(ACTION_STATUS).apply {
            `package` = ORBOT_PACKAGE
            putExtra(EXTRA_PACKAGE_NAME, context.packageName)
        }
        context.sendBroadcast(intent)

        registerStatusReceiver()
    }

    private fun registerStatusReceiver() {
        statusReceiver?.let { context.unregisterReceiver(it) }

        statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getStringExtra(EXTRA_STATUS)

                when (status) {
                    STATUS_ON -> _status.value = TorStatus.ACTIVE
                    STATUS_OFF -> _status.value = TorStatus.INSTALLED_NOT_RUNNING
                    STATUS_STARTING -> _status.value = TorStatus.STARTING
                    STATUS_STOPPING -> _status.value = TorStatus.ERROR
                }
            }
        }

        val filter = IntentFilter(ACTION_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(statusReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(statusReceiver, filter)
        }
    }

    fun unregisterReceiver() {
        statusReceiver?.let {
            context.unregisterReceiver(it)
            statusReceiver = null
        }
    }

    fun configureProcessProxy() {
        ProxySelector.setDefault(object : ProxySelector() {
            override fun select(uri: URI?): List<java.net.Proxy> {
                return if (uri != null && (uri.scheme == "http" || uri.scheme == "https")) {
                    listOf(java.net.Proxy(
                        java.net.Proxy.Type.HTTP,
                        InetSocketAddress(ORBOT_HTTP_PROXY_HOST, ORBOT_HTTP_PROXY_PORT)
                    ))
                } else {
                    listOf(java.net.Proxy.NO_PROXY)
                }
            }

            override fun connectFailed(uri: URI?, sa: InetSocketAddress?, ioe: IOException?) = Unit
        })
    }

    fun configureWebViewProxy(onReady: () -> Unit) {
        configureProcessProxy()
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val proxyConfig = ProxyConfig.Builder()
                .addProxyRule("http://$ORBOT_HTTP_PROXY_HOST:$ORBOT_HTTP_PROXY_PORT")
                .build()
            ProxyController.getInstance().setProxyOverride(
                proxyConfig,
                context.mainExecutor,
                onReady
            )
        } else {
            onReady()
        }
    }

    suspend fun testTorConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val proxy = java.net.Proxy(
                java.net.Proxy.Type.HTTP,
                InetSocketAddress(ORBOT_HTTP_PROXY_HOST, ORBOT_HTTP_PROXY_PORT)
            )
            val url = URL("https://check.torproject.org")
            val connection = url.openConnection(proxy) as HttpURLConnection
            connection.apply {
                connectTimeout = 30000
                readTimeout = 30000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            responseCode == 200 && response.contains("Congratulations")
        } catch (e: Exception) {
            false
        }
    }

    fun getProxyHost(): String = ORBOT_HTTP_PROXY_HOST
    fun getProxyPort(): Int = ORBOT_HTTP_PROXY_PORT
}
