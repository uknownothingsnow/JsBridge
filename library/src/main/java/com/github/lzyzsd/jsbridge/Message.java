package com.github.lzyzsd.jsbridge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * data of bridge
 * @author haoqing
 *
 */
public class Message {

	private String callbackId; //callbackId
	private String responseId; //responseId
	private String responseData; //responseData
	private String data; //data of message
	private String handlerName; //name of handler
	
	public String getResponseId() {
		return responseId;
	}
	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}
	public String getResponseData() {
		return responseData;
	}
	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}
	public String getCallbackId() {
		return callbackId;
	}
	public void setCallbackId(String callbackId) {
		this.callbackId = callbackId;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getHandlerName() {
		return handlerName;
	}
	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}
	
	public String toJson() {
        return new Gson().toJson(this);
	}
	
	public static Message toObject(String jsonStr) {
        return new Gson().fromJson(jsonStr, Message.class);
	}
	
	public static List<Message> toArrayList(String jsonStr){
        return new Gson().fromJson(jsonStr, new TypeToken<List<Message>>(){}.getType());
	}
}
