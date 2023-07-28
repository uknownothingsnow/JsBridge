package com.github.lzyzsd.jsbridge;

import android.content.Context;
import android.webkit.WebView;

/**
 * WebView功能接口.
 *
 * @author ZhengAn
 * @date 2019-07-01
 */
public interface IWebView {

    Context getContext();

    void loadUrl(String url);

    /**
     * 获取当前Webview
     */
    WebView getWebView();
}
