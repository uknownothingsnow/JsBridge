package com.github.lzyzsd.jsbridge;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 如果要自定义WebViewClient必须要集成此类
 * Created by bruce on 10/28/15.
 */
class BridgeWebViewClient extends WebViewClient {
    private BridgeWebView webView;
    private WebViewClient webViewClientProxy;

    public BridgeWebViewClient(BridgeWebView webView, WebViewClient webViewClient) {
        this.webView = webView;
        this.webViewClientProxy = webViewClient;
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
            if (webViewClientProxy != null) {
                return webViewClientProxy.shouldOverrideUrlLoading(view, url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    // 增加shouldOverrideUrlLoading在api》=24时
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onPageStarted(view, url, favicon);
        }
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onPageFinished(view, url);
        }
        super.onPageFinished(view, url);

        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);

        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onLoadResource(view, url);
        }
        super.onLoadResource(view, url);
    }
    @Override
    public void onPageCommitVisible(WebView view, String url) {
        if (webViewClientProxy != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                webViewClientProxy.onPageCommitVisible(view, url);
            }
        }
        super.onPageCommitVisible(view, url);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (webViewClientProxy != null) {
            webViewClientProxy.shouldInterceptRequest(view, url);
        }
        return super.shouldInterceptRequest(view, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return shouldInterceptRequest(view, request.getUrl().toString());
    }
    @Override
    public void onTooManyRedirects(WebView view, android.os.Message cancelMsg, android.os.Message continueMsg) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onTooManyRedirects(view, cancelMsg, continueMsg);
        }
        super.onTooManyRedirects(view, cancelMsg, continueMsg);
    }
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedError(view, errorCode, description, failingUrl);
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedError(view, request, error);
        }
        super.onReceivedError(view, request, error);
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedHttpError(view, request, errorResponse);
        }
        super.onReceivedHttpError(view, request, errorResponse);
    }
    @Override
    public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onFormResubmission(view, dontResend, resend);
        }
        super.onFormResubmission(view, dontResend, resend);
    }
    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if (webViewClientProxy != null) {
            webViewClientProxy.doUpdateVisitedHistory(view, url, isReload);
        }
        super.doUpdateVisitedHistory(view, url, isReload);
    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedSslError(view, handler, error);
        }
        super.onReceivedSslError(view, handler, error);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedClientCertRequest(view, request);
        }
        super.onReceivedClientCertRequest(view, request);
    }
    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }
    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        if (webViewClientProxy != null) {
            webViewClientProxy.shouldOverrideKeyEvent(view, event);
        }
        return super.shouldOverrideKeyEvent(view, event);
    }
    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onUnhandledKeyEvent(view, event);
        }
        super.onUnhandledKeyEvent(view, event);
    }
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onScaleChanged(view, oldScale, newScale);
        }
        super.onScaleChanged(view, oldScale, newScale);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        if (webViewClientProxy != null) {
            webViewClientProxy.onReceivedLoginRequest(view,realm, account, args);
        }
        super.onReceivedLoginRequest(view, realm, account, args);
    }


}