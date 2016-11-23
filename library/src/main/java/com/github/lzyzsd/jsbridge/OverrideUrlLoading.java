package com.github.lzyzsd.jsbridge;

import android.webkit.WebView;

public interface OverrideUrlLoading
    {

        boolean shouldOverrideUrlLoading(WebView view, String url);
    }