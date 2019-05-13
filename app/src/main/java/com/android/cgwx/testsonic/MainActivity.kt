package com.android.cgwx.testsonic

import android.os.Bundle

import android.app.Activity
import android.view.WindowManager
import com.tencent.sonic.sdk.SonicConfig
import com.tencent.sonic.sdk.SonicEngine
import com.tencent.sonic.sdk.SonicSessionConfig
import com.tencent.sonic.sdk.SonicSession
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.*
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.CallBackFunction
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import com.github.lzyzsd.jsbridge.DefaultHandler


/**
 * main activity of this sample
 */
class MainActivity : Activity() {
    private var sonicSession: SonicSession? = null
    internal var mUploadMessageArray: ValueCallback<Array<Uri>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val intent = intent
        val url = "file:///android_asset/www/index.html"

        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        // step 1: Initialize sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(SonicRuntimeImpl(application), SonicConfig.Builder().build())
        }

        var sonicSessionClient: SonicSessionClientImpl? = null

        // step 2: Create SonicSession
        sonicSession = SonicEngine.getInstance().createSession(url, SonicSessionConfig.Builder().build())
        if (null != sonicSession) {
            sonicSessionClient =  SonicSessionClientImpl()
            sonicSession!!.bindClient(sonicSessionClient)
        } else {
            // this only happen when a same sonic session is already running,
            // u can comment following codes to feedback as a default mode.
            throw UnknownError("create session fail!")
        }
        setContentView(R.layout.activity_main);
//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                if (sonicSession != null) {
//                    sonicSession!!.getSessionClient().pageFinish(url)
//                }
//            }
//
//            @TargetApi(21)
//            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
//                return shouldInterceptRequest(view, request.url.toString())
//            }
//
//            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
////                return if (sonicSession != null) {
////                    //step 6: Call sessionClient.requestResource when host allow the application
////                    // to return the local data .
////                    Log.e("TTT",url)
////                    sonicSession!!.getSessionClient().requestResource(url) as WebResourceResponse
////                } else null
//                return null
//            }
//        }

        webView.setDefaultHandler(DefaultHandler())

//        webView.webChromeClient = object : WebChromeClient() {
//
//        }

        var webSettings = webView.getSettings();

        // step 4: bind javascript
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
        webSettings.setJavaScriptEnabled(true);
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        var intent = Intent()
        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis());
//        webView.addJavascriptInterface( SonicJavaScriptInterface(sonicSessionClient, intent), "sonic");


        // init webview settings
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);


        // step 5: webview is ready now, just tell session client to bind
        if (sonicSessionClient != null) {
            sonicSessionClient.bindWebView(webView);
            sonicSessionClient.clientReady();
        } else { // default mode
            webView.loadUrl(url);
        }


//        webView.registerHandler("submitFromWeb") { data, function ->
//            Log.i("???", "handler = submitFromWeb, data from web = $data")
//            function.onCallBack("submitFromWeb exe, response data 中文 from Java")
//        }

//        webView.callHandler("functionInJs", "shit") { }
        btn.setOnClickListener {
            println("???")
            webView.send("发送数据给js默认接收") { data ->
                //处理js回传的数据
                Toast.makeText(this, data, Toast.LENGTH_LONG).show()
            }
        }

        btn2.setOnClickListener {
            println("???")

            webView.callHandler("functionInJs", "发送数据给js指定接收") { data ->
                //处理js回传的数据
                Toast.makeText(this, data, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        if (null != sonicSession) {
            sonicSession!!.destroy()
            sonicSession = null
        }
        super.onDestroy()
    }
}
