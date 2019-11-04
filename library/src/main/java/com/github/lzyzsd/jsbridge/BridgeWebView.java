package com.github.lzyzsd.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import androidx.collection.ArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.lzyzsd.library.BuildConfig;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge, BridgeWebViewClient.OnLoadJSListener {

	private final int URL_MAX_CHARACTER_NUM=2097152;
	public static final String toLoadJs = "WebViewJavascriptBridge.js";
	Map<String, CallBackFunction> responseCallbacks = new HashMap<String, CallBackFunction>();
	Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
	BridgeHandler defaultHandler = new DefaultHandler();
    private Map<String, OnBridgeCallback> mCallbacks = new ArrayMap<>();

    private List<Object> mMessages = new ArrayList<>();

    private BridgeWebViewClient mClient;

    private long mUniqueId = 0;

    private boolean mJSLoaded = false;

    private Gson mGson;

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
        getSettings().setUseWideViewPort(true);
//		webView.getSettings().setLoadWithOverviewMode(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        getSettings().setJavaScriptEnabled(true);
//        mContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mClient = new BridgeWebViewClient(this);
        super.setWebViewClient(mClient);
    }

    public void setGson(Gson gson) {
        mGson = gson;
    }

    public boolean isJSLoaded() {
        return mJSLoaded;
    }

    public Map<String, OnBridgeCallback> getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        mClient.setWebViewClient(client);
    }

    @Override
    public void onLoadStart() {
        mJSLoaded = false;
    }

    @Override
    public void onLoadFinished() {
        mJSLoaded = true;
        if (mMessages != null) {
            for (Object message : mMessages) {
                dispatchMessage(message);
            }
            mMessages = null;
        }
    }

    @Override
    public void sendToWeb(Object data) {
        sendToWeb(data, (OnBridgeCallback) null);
    }

    @Override
    public void sendToWeb(Object data, OnBridgeCallback responseCallback) {
        doSend(null, data, responseCallback);
    }

    /**
     * call javascript registered handler
     * 调用javascript处理程序注册
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    OnBridgeCallback
     */
    public void callHandler(String handlerName, String data, OnBridgeCallback callBack) {
        doSend(handlerName, data, callBack);
    }


    @Override
    public void sendToWeb(String function, Object... values) {
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            String jsCommand = String.format(function, values);
            jsCommand = String.format(BridgeUtil.JAVASCRIPT_STR, jsCommand);
            loadUrl(jsCommand);
        }
    }

    /**
     * 保存message到消息队列
     *
     * @param handlerName      handlerName
     * @param data             data
     * @param responseCallback OnBridgeCallback
     */
    private void doSend(String handlerName, Object data, OnBridgeCallback responseCallback) {
        if (!(data instanceof String) && mGson == null){
            return;
        }
        JSRequest request = new JSRequest();
        if (data != null) {
            request.data = data instanceof String ? (String) data : mGson.toJson(data);
        }
        if (responseCallback != null) {
            String callbackId = String.format(BridgeUtil.CALLBACK_ID_FORMAT, (++mUniqueId) + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            mCallbacks.put(callbackId, responseCallback);
            request.callbackId = callbackId;
        }
        if (!TextUtils.isEmpty(handlerName)) {
            request.handlerName = handlerName;
        }
        queueMessage(request);
    }

    /**
     * list<message> != null 添加到消息集合否则分发消息
     *
     * @param message Message
     */
    private void queueMessage(Object message) {
        if (mMessages != null) {
            mMessages.add(message);
        } else {
            dispatchMessage(message);
        }
    }

    /**
     * 分发message 必须在主线程才分发成功
     *
     * @param message Message
     */
    private void dispatchMessage(Object message) {
        if (mGson == null){
            return;
        }
        String messageJson = mGson.toJson(message);
        //escape special characters for json string  为json字符串转义特殊字符
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\')", "\\\\\'");
        messageJson = messageJson.replaceAll("%7B", URLEncoder.encode("%7B"));
        messageJson = messageJson.replaceAll("%7D", URLEncoder.encode("%7D"));
        messageJson = messageJson.replaceAll("%22", URLEncoder.encode("%22"));
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT&&javascriptCommand.length()>=URL_MAX_CHARACTER_NUM) {
				this.evaluateJavascript(javascriptCommand,null);
			}else {
				this.loadUrl(javascriptCommand);
			}
        }
    }

    public void sendResponse(Object data, String callbackId) {
        if (!(data instanceof String) && mGson == null){
            return;
        }
        if (!TextUtils.isEmpty(callbackId)) {
            final JSResponse response = new JSResponse();
            response.responseId = callbackId;
            response.responseData = data instanceof String ? (String) data : mGson.toJson(data);
            if (Thread.currentThread() == Looper.getMainLooper().getThread()){
                dispatchMessage(response);
            }else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        dispatchMessage(response);
                    }
                });
            }

        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mCallbacks.clear();
    }

    public static abstract class BaseJavascriptInterface {

        private Map<String, OnBridgeCallback> mCallbacks;

        public BaseJavascriptInterface(Map<String, OnBridgeCallback> callbacks) {
            mCallbacks = callbacks;
        }

        @JavascriptInterface
        public String send(String data, String callbackId) {
            Log.d("chromium", data + ", callbackId: " + callbackId + " " + Thread.currentThread().getName());
            return send(data);
        }

        @JavascriptInterface
        public void response(String data, String responseId) {
            Log.d("chromium", data + ", responseId: " + responseId + " " + Thread.currentThread().getName());
            if (!TextUtils.isEmpty(responseId)) {
                OnBridgeCallback function = mCallbacks.remove(responseId);
                if (function != null) {
                    function.onCallBack(data);
                }
            }
        }

        public abstract String send(String data);
    }

}
