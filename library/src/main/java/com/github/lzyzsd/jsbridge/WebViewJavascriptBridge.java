package com.github.lzyzsd.jsbridge;


public interface WebViewJavascriptBridge {
	
	void sendToWeb(String data);

	void sendToWeb(String data, OnBridgeCallback responseCallback);

	void sendToWeb(String function, Object... values);

	/**
	 * 处理从js返回的数据
	 * @param data 数据
	 * @param callbackId jsCallbackId
	 */
	void responseFromWeb(String data,String callbackId);

}
