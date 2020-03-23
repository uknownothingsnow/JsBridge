package com.github.lzyzsd.jsbridge;

import android.webkit.ValueCallback;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

/**
 * WebView功能接口.
 *
 * @author ZhengAn
 * @date 2019-07-01
 */
public interface IWebView {

    void addJavascriptInterface(Object obj, String interfaceName);

    void setWebViewClient(WebViewClient client);

    void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback);

    void loadUrl(String url);

    boolean post(Runnable runnable);
}
