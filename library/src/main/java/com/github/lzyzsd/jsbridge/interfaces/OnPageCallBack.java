package com.github.lzyzsd.jsbridge.interfaces;

import android.graphics.Bitmap;
import android.webkit.WebView;

/**
 * Created by hyy on 2017/3/18.
 */

public interface OnPageCallBack {
     void pageStartedInvoke(WebView view, String url, Bitmap favicon);
     void pageFinishedInvoke(WebView view, String url);
     void pageReceivedErrorInvoke(WebView view, int errorCode, String description, String failingUrl);
}
