
if (window.WebViewJavascriptBridge3) {
 //   return;
}
else {

var messageHandlers = {};
var responseCallbacks = {};
var uniqueId = 1;

function init(messageHandler) {
    if (WebViewJavascriptBridge3._messageHandler) {
        throw new Error('WebViewJavascriptBridge3.init called twice');
    }
    WebViewJavascriptBridge3._messageHandler = messageHandler;
}

function _doSend(message, responseCallback) {
    console.log("responseCallback:" + responseCallback);
    if (responseCallback) {
        var callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
        responseCallbacks[callbackId] = responseCallback;
        message['callbackId'] = callbackId;
    }
    console.log("sending:" + JSON.stringify(message));
    _WebViewJavascriptBridge._handleMessageFromJs(message.data || null, message.responseId || null,
        message.responseData || null, message.callbackId || null, message.handlerName || null);

}

function send(data, responseCallback) {
    _doSend({data: data}, responseCallback);
}

function registerHandler(handlerName, handler) {
    messageHandlers[handlerName] = handler;
}

function callHandler(handlerName, data, responseCallback) {
    _doSend({handlerName: handlerName, data: data}, responseCallback);
}



function _dispatchMessageFromJava(messageJSON) {
    var message = JSON.parse(messageJSON);
    var messageHandler;

    if (message.responseId) {
        var responseCallback = responseCallbacks[message.responseId];
        if (!responseCallback) {
            return;
        }
        responseCallback(message.responseData);
        delete responseCallbacks[message.responseId];
    }
    else {
        var responseCallback;
        if (message.callbackId) {
            var callbackResponseId = message.callbackId;
            responseCallback = function(responseData) {
                _doSend({
                    responseId: callbackResponseId,
                    responseData: responseData
                });
            };
        }

        var handler = WebViewJavascriptBridge3._messageHandler;
        if (message.handlerName) {
            handler = messageHandlers[message.handlerName];
        }
        try {
            handler(message.data, responseCallback);
        } catch(exception) {
            if (typeof console !='undefined') {
                console.log("WebViewJavascriptBridge3: WARNING: javascript handler threw.", message, exception);
            }
        }
    }
}

function _handleMessageFromJava(messageJSON) {
    _dispatchMessageFromJava(messageJSON);
}
window.WebViewJavascriptBridge3 = {
    'init': init,
    'send': send,
    'registerHandler': registerHandler,
    'callHandler': callHandler,
    '_handleMessageFromJava': _handleMessageFromJava
};

}
