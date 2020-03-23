package com.github.lzyzsd.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.lzyzsd.library.BuildConfig;
import com.google.gson.Gson;

import java.net.URLEncoder;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge, IWebView {

    private BridgeHelper mBridgeHelper;

    public BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BridgeWebView(Context context) {
        super(context);
        init();
    }

    private void init() {

        clearCache(true);
        WebSettings webSettings = getSettings();
        webSettings.setUseWideViewPort(true);
//		webView.getSettings().setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptEnabled(true);

//        mContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mBridgeHelper = new BridgeHelper(this);
    }

    @NonNull
    final BridgeHelper getBridgeHelper() {
        return mBridgeHelper;
    }

    public void setGson(Gson gson) {
        mBridgeHelper.setGson(gson);
    }

    public boolean isJSLoaded() {
        return mBridgeHelper.isJSLoaded();
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (client instanceof BridgeWebViewClient){
            super.setWebViewClient(client);
        }else {
            Log.w("BridgeWebView", "this client is deprecated, you should use BridgeWebViewClient");
            super.setWebViewClient(new WebViewClientProxy(mBridgeHelper, client));
        }
    }


    @Override
    public void sendToWeb(Object data) {
        mBridgeHelper.sendToWeb(data);
    }

    @Override
    public void sendToWeb(Object data, CallBackFunction responseCallback) {
        mBridgeHelper.sendToWeb(data, responseCallback);
    }

    /**
     * call javascript registered handler
     * 调用javascript处理程序注册
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    OnBridgeCallback
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        mBridgeHelper.callHandler(handlerName, data, callBack);
    }

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    @Deprecated
    public void setDefaultHandler(BridgeHandler handler) {
        mBridgeHelper.setDefaultHandler(handler);
    }

    /**
     * register handler,so that javascript can call it
     * 注册处理程序,以便javascript调用它
     *
     * @param handlerName handlerName
     * @param handler     BridgeHandler
     */
    @Deprecated
    public void registerHandler(String handlerName, BridgeHandler handler) {
        mBridgeHelper.registerHandler(handlerName, handler);
    }

    /**
     * unregister handler
     *
     * @param handlerName
     */
    @Deprecated
    public void unregisterHandler(String handlerName) {
        mBridgeHelper.unregisterHandler(handlerName);
    }

    /**
     * Experimental. Use with caution
     *
     * @param function method name
     * @param values   method parameters
     */
    @Override
    public void sendToWeb(String function, Object... values) {
        mBridgeHelper.sendToWeb(function, values);
    }


    /**
     * Experimental. Use with caution
     *
     * @param function method name
     * @param callback value callback
     * @param values   method parameters
     */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendToWeb(String function, ValueCallback<String> callback, Object... values) {
        mBridgeHelper.sendToWeb(function, callback, values);
    }


    @Override
    public void destroy() {
        super.destroy();
        mBridgeHelper.destroy();
    }




}
