package com.github.lzyzsd.jsbridge.example;

import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.github.lzyzsd.jsbridge.BaseJavascriptInterface;
import com.github.lzyzsd.jsbridge.BridgeHelper;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import java.util.Map;

/**
 * Created on 2019/7/10.
 * Author: bigwang
 * Description:
 */
public class MainJavascriptInterface extends BaseJavascriptInterface {


    public MainJavascriptInterface(@NonNull BridgeWebView webView) {
        super(webView);
    }

    public MainJavascriptInterface(BridgeHelper helper) {
        super(helper);
    }


    public String send(String data) {
        return "it is default response";
    }


    @JavascriptInterface
    public String submitFromWeb(String data, String callbackId) {
        Log.d("chromium data", data + ", callbackId: " + callbackId + " " + Thread.currentThread().getName());
//        mWebView.sendResponse("submitFromWeb response", callbackId);
        return "submitFromWeb response";
    }
}
