package com.github.lzyzsd.jsbridge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Message {
    public String responseId;
    public String responseData;
    public String callbackId;
    public String data;
    public String handlerName;

    public static List<Message> toArrayList(String data) {
        return new Gson().fromJson(data, new TypeToken<List<Message>>(){}.getType());
    }

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

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setCallbackId(String callbackStr) {
        this.callbackId = callbackStr;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
