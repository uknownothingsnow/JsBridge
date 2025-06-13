package com.github.lzyzsd.jsbridge.example;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.OnBridgeCallback;
import com.github.lzyzsd.jsbridge.WebViewJavascriptBridge;

import java.util.Map;

/**
 * Created on 2019/7/10.
 * Author: bigwang
 * Description:
 */
public class MainJavascriptInterface extends BridgeWebView.BaseJavascriptInterface {

    //WebJSbridge
    private WebViewJavascriptBridge mWebView;

    public MainJavascriptInterface(Map<String, OnBridgeCallback> callbacks, WebViewJavascriptBridge webView) {
        super(callbacks);
        mWebView = webView;
    }

    public MainJavascriptInterface(Map<String, OnBridgeCallback> callbacks, Map<String, OnBridgeCallback> persistentCallbacks, WebViewJavascriptBridge webView) {
        super(callbacks, persistentCallbacks);
        mWebView = webView;
    }

    public MainJavascriptInterface(Map<String, OnBridgeCallback> callbacks) {
        super(callbacks);
    }

    @Override
    public String send(String data) {
        return "it is default response";
    }


    @JavascriptInterface
    public void submitFromWeb(String data, String callbackId) {
        Log.d("MainJavascriptInterface", data + ", callbackId: " + callbackId + " " + Thread.currentThread().getName());
        mWebView.responseFromWeb("submitFromWeb response", callbackId);
    }
}
