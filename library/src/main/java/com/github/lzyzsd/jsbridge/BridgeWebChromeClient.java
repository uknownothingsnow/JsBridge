package com.github.lzyzsd.jsbridge;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

class BridgeWebChromeClient extends WebChromeClient {
    private WebChromeClient webChromeClientProxy;
    public BridgeWebChromeClient(WebChromeClient webChromeClient) {
        this.webChromeClientProxy = webChromeClient;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onProgressChanged(view, newProgress);
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onReceivedTitle(view, title);
        }
        super.onReceivedTitle(view, title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onReceivedIcon(view, icon);
        }
        super.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onReceivedTouchIconUrl(view, url, precomposed);
        }
        super.onReceivedTouchIconUrl(view, url, precomposed);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onShowCustomView(view, callback);
        }
        super.onShowCustomView(view, callback);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onShowCustomView(view, requestedOrientation, callback);
        }
        super.onShowCustomView(view, requestedOrientation, callback);
    }

    @Override
    public void onHideCustomView() {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onHideCustomView();
        }
        super.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onRequestFocus(view);
        }
        super.onRequestFocus(view);
    }

    @Override
    public void onCloseWindow(WebView window) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onCloseWindow(window);
        }
        super.onCloseWindow(window);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onJsAlert(view, url, message, result);
        }
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onJsConfirm(view, url, message, result);
        }
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onJsPrompt(view, url, message, defaultValue, result);
        }
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onJsBeforeUnload(view, url, message, result);
        }
        return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
        }
        super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
        }
        super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onGeolocationPermissionsShowPrompt(origin, callback);
        }
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onGeolocationPermissionsHidePrompt();
        }
        super.onGeolocationPermissionsHidePrompt();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onPermissionRequest(request);
        }
        super.onPermissionRequest(request);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onPermissionRequestCanceled(request);
        }
        super.onPermissionRequestCanceled(request);
    }

    @Override
    public boolean onJsTimeout() {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onJsTimeout();
        }
        return super.onJsTimeout();
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onConsoleMessage(message, lineNumber, sourceID);
        }
        super.onConsoleMessage(message, lineNumber, sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onConsoleMessage(consoleMessage);
        }
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.getDefaultVideoPoster();
        }
        return super.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.getVideoLoadingProgressView();
        }
        return super.getVideoLoadingProgressView();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.getVisitedHistory(callback);
        }
        super.getVisitedHistory(callback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (webChromeClientProxy != null) {
            webChromeClientProxy.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }
}
