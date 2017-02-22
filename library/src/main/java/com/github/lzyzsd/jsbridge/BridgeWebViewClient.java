package com.github.lzyzsd.jsbridge;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private BridgeWebView webView;
    private WebViewClient extra;

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
        } else {
            if (extra != null) {
                return extra.shouldOverrideUrlLoading(view, url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (extra != null) {
            extra.onPageStarted(view, url, favicon);
        } else {
            super.onPageStarted(view, url, favicon);
        }
    }

    /**
     * it is essential to set startUpMessages to null,
     * otherwise all subsequent messages would not be consumed.
     * check {@link BridgeWebView#queueMessage(Message)}  for more details
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (extra != null) {
            extra.onPageFinished(view, url);
        } else {
            super.onPageFinished(view, url);
        }

        if (BridgeWebView.toLoadJs != null) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        }

        if (webView.getStartupMessage() != null) {
            //todo: synchronize these operations
            List<Message> copy = new ArrayList<>(webView.getStartupMessage().size());
            copy.addAll(webView.getStartupMessage());
            webView.setStartupMessage(null);

            for (Message m : copy) {
                webView.dispatchMessage(m);
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (extra != null) {
            extra.onReceivedError(view, errorCode, description, failingUrl);
        } else {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    public void setExtraWebViewClient(WebViewClient client) {
        extra = client;
    }
}