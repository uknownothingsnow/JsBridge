package com.github.lzyzsd.jsbridge.example;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeHelper;
import com.github.lzyzsd.jsbridge.IWebView;
import com.github.lzyzsd.jsbridge.OnBridgeCallback;
import com.github.lzyzsd.jsbridge.WebViewJavascriptBridge;
import com.google.gson.Gson;

import java.util.Map;

/**
 * 采用BridgeHelper集成JsBridge功能示例.定制WebView,可只添加实际需要的JsBridge接口.
 *
 * @author ZhengAn
 * @date 2019-07-07
 */
@SuppressLint("SetJavaScriptEnabled")
public class CustomWebView extends WebView implements WebViewJavascriptBridge, IWebView {

    private BridgeHelper bridgeHelper;

    private Gson mGson;

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomWebView(Context context) {
        super(context);
        init();
    }

    private void init() {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        bridgeHelper = new BridgeHelper(this);
        this.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String s) {
                bridgeHelper.onPageFinished();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                return bridgeHelper.shouldOverrideUrlLoading(s);
            }
        });
    }

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        bridgeHelper.setDefaultHandler(handler);
    }

    /**
     * register handler,so that javascript can call it
     * 注册处理程序,以便javascript调用它
     *
     * @param handlerName handlerName
     * @param handler     BridgeHandler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        bridgeHelper.registerHandler(handlerName, handler);
    }

    /**
     * unregister handler
     *
     * @param handlerName
     */
    public void unregisterHandler(String handlerName) {
        bridgeHelper.unregisterHandler(handlerName);
    }

    /**
     * call javascript registered handler
     * 调用javascript处理程序注册
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    CallBackFunction
     */
    public void callHandler(String handlerName, String data, OnBridgeCallback callBack) {
        bridgeHelper.callHandler(handlerName, data, callBack);
    }

    @Override
    public void sendToWeb(String data) {
        sendToWeb(data, (OnBridgeCallback) null);
    }

    @Override
    public void sendToWeb(String data, OnBridgeCallback responseCallback) {
        bridgeHelper.sendToWeb(data, responseCallback);
    }

    @Override
    public void sendToWeb(String function, Object... values) {
        bridgeHelper.sendToWeb(function, values);
    }

    @Override
    public void responseFromWeb(String data, String callbackId) {
        bridgeHelper.responseFromWeb(data,callbackId);
    }

    public void setGson(Gson gson){
        this.mGson = gson;
    }

    public Map<String, OnBridgeCallback> getCallbacks() {
        return bridgeHelper.getCallbacks();
    }

    @Override
    public WebView getWebView() {
        return this;
    }
}
