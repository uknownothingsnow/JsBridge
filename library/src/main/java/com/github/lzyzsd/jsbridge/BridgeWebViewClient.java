package com.github.lzyzsd.jsbridge;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private BridgeWebView webView;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        }
//        else {
//            return super.shouldOverrideUrlLoading(view, url);
//        }
        /** add  by arthas statr @20160203**/
        else if (url.startsWith("tel:"))
        {
            String tel = url.substring(4);
            Intent telIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
            webView.getContext().startActivity(telIntent);
            return true;
        } else
        {
            if (overrideUrlLoading == null)
            {
                return super.shouldOverrideUrlLoading(view, url);
            } else
            {
                return overrideUrlLoading.shouldOverrideUrlLoading(view, url);
            }
        }
        /** add  by arthas end @20160203**/
    }
    /** add  by arthas statr @20160203**/
    OverrideUrlLoading overrideUrlLoading;

    public void setOverrideUrlLoading(OverrideUrlLoading overrideUrlLoading)
    {
        this.overrideUrlLoading = overrideUrlLoading;
    }
    /** add  by arthas end @20160203**/


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (BridgeWebView.toLoadJs != null) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        }

        //
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}