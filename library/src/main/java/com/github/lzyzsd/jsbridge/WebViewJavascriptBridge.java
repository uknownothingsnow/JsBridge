package com.github.lzyzsd.jsbridge;


import android.webkit.ValueCallback;

public interface WebViewJavascriptBridge {
	
	void sendToWeb(Object data);

	void sendToWeb(Object data, CallBackFunction responseCallback);

	void sendToWeb(String function, Object... values);

	void sendToWeb(String function, ValueCallback<String> callback, Object... values);

}
