package com.github.lzyzsd.jsbridge;

import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.webkit.ValueCallback;

import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;

import com.google.gson.Gson;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JsBridge辅助类,帮助集成JsBridge功能.
 *
 * @author ZhengAn
 * @date 2019-06-30
 */
public class BridgeHelper implements WebViewJavascriptBridge, WebViewClientProxy.OnLoadJSListener {

    @Deprecated
    private Map<String, BridgeHandler> mMessageHandlers = new ArrayMap<>();
    private Map<String, CallBackFunction> mCallbacks = new ArrayMap<>();
    @Deprecated
    private BridgeHandler mDefaultHandler = new DefaultHandler();

    private List<Object> mMessages = new ArrayList<>();

    private long mUniqueId = 0;

    private boolean mJSLoaded = false;

    private Gson mGson = new Gson();

    private IWebView webView;

    public BridgeHelper(IWebView webView) {
        this.webView = webView;
        webView.addJavascriptInterface(new BaseJavascriptInterface(this), "android");
        BridgeWebViewClient client = new BridgeWebViewClient() {

            @Override
            public void onBridgeLoadStart() {
                mJSLoaded = false;
            }

            @Override
            public void onBridgeLoadFinished() {
                mJSLoaded = true;
                if (mMessages != null) {
                    for (Object message : mMessages) {
                        dispatchMessage(message);
                    }
                    mMessages = null;
                }
            }
        };
        webView.setWebViewClient(client);
    }

    public void setGson(Gson gson) {
        mGson = gson;
    }

    public boolean isJSLoaded() {
        return mJSLoaded;
    }

    final Map<String, CallBackFunction> getResponseCallbacks() {
        return mCallbacks;
    }

    final Map<String, BridgeHandler> getMessageHandlers() {
        return mMessageHandlers;
    }

    final BridgeHandler getDefaultHandler() {
        return mDefaultHandler;
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
        sendToWeb(data, (CallBackFunction) null);
    }

    @Override
    public void sendToWeb(Object data, CallBackFunction responseCallback) {
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
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
    }

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    @Deprecated
    public void setDefaultHandler(BridgeHandler handler) {
        this.mDefaultHandler = handler;
    }

    /**
     * register handler,so that javascript can call it
     * 注册处理程序,以便javascript调用它
     *
     * @param handlerName handlerName
     * @param handler     BridgeHandler
     */
    @Deprecated
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            // 添加至 Map<String, BridgeHandler>
            mMessageHandlers.put(handlerName, handler);
        }
    }

    /**
     * unregister handler
     *
     * @param handlerName
     */
    @Deprecated
    public void unregisterHandler(String handlerName) {
        if (handlerName != null) {
            mMessageHandlers.remove(handlerName);
        }
    }

    /**
     * Experimental. Use with caution
     *
     * @param function method name
     * @param values   method parameters
     */
    @Override
    public void sendToWeb(String function, Object... values) {
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            String jsCommand = String.format(function, values);
            jsCommand = String.format(BridgeUtil.JAVASCRIPT_STR, jsCommand);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(jsCommand, null);
            } else {
                webView.loadUrl(jsCommand);
            }
        }
    }


    /**
     * Experimental. Use with caution
     *
     * @param function method name
     * @param callback value callback
     * @param values   method parameters
     */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendToWeb(String function, ValueCallback<String> callback, Object... values) {
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            String jsCommand = String.format(function, values);
            jsCommand = String.format(BridgeUtil.JAVASCRIPT_STR, jsCommand);
            webView.evaluateJavascript(jsCommand, callback);
        }
    }


    /**
     * 保存message到消息队列
     *
     * @param handlerName      handlerName
     * @param data             data
     * @param responseCallback OnBridgeCallback
     */
    private void doSend(String handlerName, Object data, CallBackFunction responseCallback) {
        if (!(data instanceof String) && mGson == null) {
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
        if (mGson == null) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(javascriptCommand, null);
            } else {
                webView.loadUrl(javascriptCommand);
            }
        }
    }

    public void sendResponse(Object data, String callbackId) {
        if (!(data instanceof String) && mGson == null) {
            return;
        }
        if (!TextUtils.isEmpty(callbackId)) {
            final JSResponse response = new JSResponse();
            response.responseId = callbackId;
            response.responseData = data instanceof String ? (String) data : mGson.toJson(data);
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                dispatchMessage(response);
            } else {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        dispatchMessage(response);
                    }
                });
            }

        }
    }

    public void destroy() {
        mCallbacks.clear();
        mMessageHandlers.clear();
    }


}
