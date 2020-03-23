package com.github.lzyzsd.jsbridge;


import android.webkit.ValueCallback;

interface WebViewJavascriptBridge {
	
	void sendToWeb(Object data);

	void sendToWeb(Object data, OnBridgeCallback responseCallback);

	void sendToWeb(String function, Object... values);

	void sendToWeb(String function, ValueCallback<String> callback, Object... values);

}
