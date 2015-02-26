package com.github.lzyzsd.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

	private final String TAG = "BridgeWebView";

	String toLoadJs = null;
	Map<String, CallBackFunction> responseCallbacks = new HashMap<String, CallBackFunction>();
	Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
	BridgeHandler defaultHandler = new DefaultHandler();

	List<Message> startupMessage = new ArrayList<Message>();
	long uniqueId = 0;

	public BridgeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public BridgeWebView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 
	 * @param handler
	 *            默认的handler,负责处理js端没有指定handlerName的消息,若js端有指定handlerName,
	 *            则由native端注册的指定处理
	 */
	public void initContext(BridgeHandler handler) {
        this.toLoadJs = "WebViewJavascriptBridge.js";
		if (handler != null) {
			this.defaultHandler = handler;
		}
	}

    private void init(Context context) {
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
		this.setWebViewClient(new BridgeWebViewClient());
	}

	private void handlerReturnData(String url) {
		String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
		CallBackFunction f = responseCallbacks.get(functionName);
		String data = BridgeUtil.getDataFromReturnUrl(url);
		Log.i(TAG, "handlerReturnData " + "functionName = " + functionName + " f = " + f + " data = " + data);
		if (f != null) {
			Log.i(TAG, "in handlerReturnData, f = " + f);
			f.onCallBack(data);
			responseCallbacks.remove(functionName);
			return;
		}
	}

	class BridgeWebViewClient extends WebViewClient {

        @Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				url = URLDecoder.decode(url, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
				handlerReturnData(url);
				return true;
			} else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
				flushMessageQueue();
				return true;
			} else {
				return super.shouldOverrideUrlLoading(view, url);
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			if (toLoadJs != null) {
				BridgeUtil.webViewLoadLocalJs(view, toLoadJs);
			}

			//
			if (startupMessage != null) {
				for (Message m : startupMessage) {
					dispatchMessage(m);
				}
				startupMessage = null;
			}
		}

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

	@Override
	public void send(String data) {
		send(data, null);
	}

	@Override
	public void send(String data, CallBackFunction responseCallback) {
		doSend(data, responseCallback, null);
	}

	private void doSend(String data, CallBackFunction responseCallback, String handlerName) {
		Message m = new Message();
		if (!TextUtils.isEmpty(data)) {
			m.setData(data);
		}
		if (responseCallback != null) {
			String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
			responseCallbacks.put(callbackStr, responseCallback);
			m.setCallbackId(callbackStr);
		}
		if (!TextUtils.isEmpty(handlerName)) {
			m.setHandlerName(handlerName);
		}
		queueMessage(m);
	}

	private void queueMessage(Message m) {
		if (startupMessage != null) {
			startupMessage.add(m);
		} else {
			dispatchMessage(m);
		}
	}

	private void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

	public void flushMessageQueue() {
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new CallBackFunction() {

				@Override
				public void onCallBack(String data) {
					// deserializeMessage
					List<Message> list = null;
					try {
						list = Message.toArrayList(data);
					} catch (Exception e) {
						return;
					}
					if (list == null || list.size() == 0) {
						return;
					}
					for (int i = 0; i < list.size(); i++) {
						Message m = list.get(i);
						String responseId = m.getResponseId();
						// 是否是response
						if (!TextUtils.isEmpty(responseId)) {
							CallBackFunction function = responseCallbacks.get(responseId);
							String responseData = m.getResponseData();
							function.onCallBack(responseData);
							responseCallbacks.remove(responseId);
						} else {
							CallBackFunction responseFunction = null;
							// 是否是callbackId
							final String callbackId = m.getCallbackId();
							if (!TextUtils.isEmpty(callbackId)) {
								responseFunction = new CallBackFunction() {
									@Override
									public void onCallBack(String data) {
										Message responseMsg = new Message();
										responseMsg.setResponseId(callbackId);
										responseMsg.setResponseData(data);
										queueMessage(responseMsg);
									}
								};
							} else {
								responseFunction = new CallBackFunction() {
									@Override
									public void onCallBack(String data) {
										// do nothing
									}
								};
							}
							BridgeHandler handler;
							if (!TextUtils.isEmpty(m.getHandlerName())) {
								handler = messageHandlers.get(m.getHandlerName());
							} else {
								handler = defaultHandler;
							}
							handler.handler(m.getData(), responseFunction);
						}
					}
				}
			});
		}
	}

	public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
		this.loadUrl(jsUrl);
		responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
	}

	/**
	 * 注册handler,方便web调用
	 * 
	 * @param handlerName
	 * @param handler
	 */
	public void registerHandler(String handlerName, BridgeHandler handler) {
		if (handler != null) {
			messageHandlers.put(handlerName, handler);
		}
	}

	/**
	 * 调用web的handler
	 * 
	 * @param data
	 * @param callBack
	 * @param handlerName
	 */
	public void callHandler(String data, CallBackFunction callBack, String handlerName) {
        doSend(data, callBack, handlerName);
	}
}
