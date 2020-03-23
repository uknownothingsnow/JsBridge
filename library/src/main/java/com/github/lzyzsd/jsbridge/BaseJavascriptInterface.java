package com.github.lzyzsd.jsbridge;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * Created on 2020/3/23
 * Author: bigwang
 * Description:
 */
public class BaseJavascriptInterface {

    private Map<String, CallBackFunction> mCallbacks;
    private Map<String, BridgeHandler> mMessageHandlers;
    private BridgeHelper mBridgeHelper;

    public BaseJavascriptInterface(@NonNull BridgeHelper helper) {
        mCallbacks = helper.getResponseCallbacks();
        mMessageHandlers = helper.getMessageHandlers();
        mBridgeHelper = helper;
    }

    public BaseJavascriptInterface(@NonNull BridgeWebView webView){
        this(webView.getBridgeHelper());
    }


    @JavascriptInterface
    public final String send(String data, String callbackId) {
        Log.d("chromium", data + ", callbackId: " + callbackId + " " + Thread.currentThread().getName());
        return receiveMessage(data, callbackId);
    }

    @JavascriptInterface
    @Deprecated
    public final void send(String handlerName, String data, final String callbackId) {
        Log.d("chromium", "handlerName: " + handlerName + " " + data + ", callbackId: " + callbackId + " " + Thread.currentThread().getName());
        BridgeHandler handler = mMessageHandlers.get(handlerName);
        final BridgeHelper webView = mBridgeHelper;
        if (handler == null && webView != null) {
            handler = webView.getDefaultHandler();
        }
        if (handler != null) {
            handler.handler(data, new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    if (webView != null) {
                        webView.sendResponse(data, callbackId);
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public final void response(String data, String responseId) {
        Log.d("chromium", data + ", responseId: " + responseId + " " + Thread.currentThread().getName());
        responseFormWeb(data, responseId);
    }

    protected String receiveMessage(String data, final String callbackId) {
        BridgeHandler handler = null;
        final BridgeHelper webView = mBridgeHelper;
        if (webView != null) {
            handler = webView.getDefaultHandler();
        }
        if (handler != null) {
            handler.handler(data, new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    webView.sendResponse(data, callbackId);
                }
            });
        }
        return null;
    }

    protected void responseFormWeb(String data, String responseId) {
        if (!TextUtils.isEmpty(responseId)) {
            CallBackFunction function = mCallbacks.remove(responseId);
            if (function != null) {
                function.onCallBack(data);
            }
        }
    }

}
