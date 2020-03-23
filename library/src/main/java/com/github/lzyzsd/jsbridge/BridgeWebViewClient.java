package com.github.lzyzsd.jsbridge;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 如果要自定义WebViewClient必须要集成此类
 * Created by bruce on 10/28/15.
 */

public class BridgeWebViewClient extends WebViewClient {


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        onBridgeLoadStart();
        BridgeUtil.webViewLoadLocalJs(view, BridgeUtil.JAVA_SCRIPT);
        onBridgeLoadFinished();
    }

    public void onBridgeLoadStart() {

    }

    public void onBridgeLoadFinished() {

    }

}