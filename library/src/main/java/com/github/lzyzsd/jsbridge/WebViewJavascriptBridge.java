package com.github.lzyzsd.jsbridge;


public interface WebViewJavascriptBridge {
	
	void send(String data);
	void send(String data, CallBackFunction responseCallback);
}
