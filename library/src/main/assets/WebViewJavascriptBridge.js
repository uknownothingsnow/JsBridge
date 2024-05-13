//notation: js file can only use this kind of comments
//since comments will cause error when use in webview.loadurl,
//comments will be remove by java use regexp
(function() {
    if (window.WebViewJavascriptBridge && window.WebViewJavascriptBridge.inited) {
        return;
    }

    var receiveMessageQueue = [];
    var messageHandlers = {};
    var sendMessageQueue = [];

    var responseCallbacks = {};
    var uniqueId = 1;

    var lastCallTime = 0;
    var stoId = null;
    var FETCH_QUEUE_INTERVAL = 20;
    var messagingIframe;
    var CUSTOM_PROTOCOL_SCHEME = "yy";
    var QUEUE_HAS_MESSAGE = "__QUEUE_MESSAGE__";

    // 创建消息index队列iframe
    function _createQueueReadyIframe() {
        messagingIframe = document.createElement('iframe');
        messagingIframe.style.display = 'none';
        messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;
        document.documentElement.appendChild(messagingIframe);
    }
    //创建消息体队列iframe
    function _createQueueReadyIframe4biz() {
        bizMessagingIframe = document.createElement('iframe');
        bizMessagingIframe.style.display = 'none';
        document.documentElement.appendChild(bizMessagingIframe);
    }
    //set default messageHandler  初始化默认的消息线程
    function init(messageHandler) {
        if (WebViewJavascriptBridge._messageHandler) {
            throw new Error('WebViewJavascriptBridge.init called twice');
        }
        _createQueueReadyIframe();
        _createQueueReadyIframe4biz();
        WebViewJavascriptBridge._messageHandler = messageHandler;
        var receivedMessages = receiveMessageQueue;
        receiveMessageQueue = null;
        for (var i = 0; i < receivedMessages.length; i++) {
            _dispatchMessageFromNative(receivedMessages[i]);
        }
        WebViewJavascriptBridge.inited = true;
    }

    // 发送
    function send(data, responseCallback) {
        _doSend('send', data, responseCallback);
    }

    // 注册线程 往数组里面添加值
    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }

    function removeHandler(handlerName, handler) {
        delete messageHandlers[handlerName];
    }

    // 调用线程
    function callHandler(handlerName, data, responseCallback) {
        // 如果方法不需要参数，只有回调函数，简化JS中的调用
        if (arguments.length == 2 && typeof data == 'function') {
			responseCallback = data;
			data = null;
		}
        _doSend(handlerName, data, responseCallback);
    }

    //sendMessage add message, 触发native处理 sendMessage
    function _doSend(handlerName, message, responseCallback) {
        var callbackId;
        if(typeof responseCallback === 'string'){
            callbackId = responseCallback;
        } else if (responseCallback) {
            callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
            responseCallbacks[callbackId] = responseCallback;
            message.callbackId = callbackId;
        }else{
            callbackId = '';
        }
        try {
             var fn = eval('WebViewJavascriptBridge.' + handlerName);
         } catch(e) {
             console.log(e);
         }
         if (typeof fn === 'function'){
             var responseData = fn.call(WebViewJavascriptBridge, JSON.stringify(message), callbackId);
             if(responseData){
                 responseCallback = responseCallbacks[callbackId];
                 if (!responseCallback) {
                     return;
                  }
                 responseCallback(responseData);
                 delete responseCallbacks[callbackId];
             }
         }

        sendMessageQueue.push(message);
        messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;
    }

    // 提供给native调用,该函数作用:获取sendMessageQueue返回给native,由于android不能直接获取返回的内容,所以使用url shouldOverrideUrlLoading 的方式返回内容
    function _fetchQueue() {
        // 空数组直接返回
        if (sendMessageQueue.length === 0) {
          return;
        }

        // _fetchQueue 的调用间隔过短，延迟调用
        if (new Date().getTime() - lastCallTime < FETCH_QUEUE_INTERVAL) {
          if (!stoId) {
            stoId = setTimeout(_fetchQueue, FETCH_QUEUE_INTERVAL);
          }
          return;
        }

        lastCallTime = new Date().getTime();
        stoId = null;
        var messageQueueString = JSON.stringify(sendMessageQueue);
        sendMessageQueue = [];
        //android can't read directly the return data, so we can reload iframe src to communicate with java
        bizMessagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://return/_fetchQueue/' + encodeURIComponent(messageQueueString);
    }

    //提供给native使用,
    function _dispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            var message = JSON.parse(messageJSON);
            var responseCallback;
            //java call finished, now need to call js callback function
            if (message.responseId) {
                responseCallback = responseCallbacks[message.responseId];
                if (!responseCallback) {
                    return;
                }
                responseCallback(message.responseData);
                delete responseCallbacks[message.responseId];
            } else {
                //直接发送
                if (message.callbackId) {
                    var callbackResponseId = message.callbackId;
                    responseCallback = function(responseData) {
                        _doSend('response', responseData, callbackResponseId);
                    };
                }

                var handler = WebViewJavascriptBridge._messageHandler;
                if (message.handlerName) {
                    handler = messageHandlers[message.handlerName];
                }
                //查找指定handler
                try {
                    handler(message.data, responseCallback);
                } catch (exception) {
                    if (typeof console != 'undefined') {
                        console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                    }
                }
            }
        });
    }

    //提供给native调用,receiveMessageQueue 在会在页面加载完后赋值为null,所以
    function _handleMessageFromNative(messageJSON) {
        if (receiveMessageQueue) {
            receiveMessageQueue.push(messageJSON);
        }
        _dispatchMessageFromNative(messageJSON);

    }

    WebViewJavascriptBridge.init = init;
    WebViewJavascriptBridge.doSend = send;
    WebViewJavascriptBridge.registerHandler = registerHandler;
    WebViewJavascriptBridge.callHandler = callHandler;
    WebViewJavascriptBridge._handleMessageFromNative = _handleMessageFromNative;
    WebViewJavascriptBridge._fetchQueue = _fetchQueue;

    var readyEvent = document.createEvent('Events');
    var jobs = window.WVJBCallbacks || [];
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = WebViewJavascriptBridge;
    window.WVJBCallbacks = [];
    jobs.forEach(function (job) {
        job(WebViewJavascriptBridge)
    });
    document.dispatchEvent(readyEvent);
})();
