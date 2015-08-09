var scProxy;
function connectWebViewJavascriptBridge(callback) {
    callback(window.WebViewJavascriptBridge3)
}

function getSocketStateBridge() {
    if (scProxy) {
        return scProxy.getState();
    }
    else {
        return 'NOT_INIT';
    }
}
function getSubscriptionsBridge(includePending) {
    includePending = includePending || false;
    return scProxy.subscriptions(includePending);
}
function getSocketId() {
    return scProxy.id;
}

connectWebViewJavascriptBridge(function(bridge) {
    bridge.registerHandler("connectHandler", function(data) {
        var data = JSON.parse(data);
        scProxy = socketCluster.connect({
            hostname: data.hostname,
            secure: data.secure === "true" ? true : false,
            port: data.port
        });

        scProxy.removeAllListeners('error');
//        scProxy.on('error', function (err) {
//            bridge.callHandler('onErrorHandler', {'data': err.message}, function(response) {});
//        });
//        scProxy.removeAllListeners('connect');
//        scProxy.on('connect', function () {
//          bridge.callHandler('onConnectHandler', {}, function(response) {});
//        });
//        scProxy.removeAllListeners('disconnect');
//        scProxy.on('disconnect', function () {
//          bridge.callHandler('onDisconnectedHandler', {}, function(response) {});
//        });
//
//        scProxy.removeAllListeners('kickOut');
//        scProxy.on('kickOut', function (data) {
//          bridge.callHandler('onKickoutHandler', data, function(response) {});
//        });
//
//        scProxy.removeAllListeners('subscribe');
//        scProxy.on('subscribe', function (data) {
//          bridge.callHandler('onSubscribeHandler', data, function(response) {});
//        });
//
//        scProxy.removeAllListeners('subscribeFail');
//        scProxy.on('subscribeFail', function (data) {
//          bridge.callHandler('onSubscribeFailHandler', data, function(response) {});
//        });
//
//        scProxy.removeAllListeners('unsubscribe');
//            scProxy.on('unsubscribe', function (data) {
//              bridge.callHandler('onUnsubscribeHandler', data, function(response) {});
//            });
//	    });
//
//	    bridge.registerHandler("disconnectHandler", function(data) {
//	    	 scProxy.disconnect();
//
//	    });
    });

    bridge.registerHandler("onEventHandler", function(data, responseCallback) {
        data = JSON.parse(data);
        var eventName = data.event;
        scProxy.on(eventName, function(data) {
            console.log("eventName", eventName, data);
            bridge.callHandler('onEventReceivedFromSocketCluster', JSON.stringify({
                'event': eventName,
                'data': JSON.stringify(data)
            }));
        });
    });

    bridge.registerHandler("publishHandler", function(packet) {
        var channelName = packet.channel;
        if (!channelName) {
            return;
        }
        scProxy.publish(channelName, packet.data);

    });
    bridge.registerHandler("subscribeHandler", function(packet) {
        var channelName = packet.channel;
        if (!channelName) {
            return;
        }

        if (!scProxy.isSubscribed(channelName)) {
            scProxy.watch(channelName, function(publishedData) {
                bridge.callHandler('onChannelReceivedEventFromSocketCluster', {
                    'channel': channelName,
                    'data': publishedData
                });
            });
        }
        scProxy.subscribe(channelName);
    });
    bridge.registerHandler("unsubscribeHandler", function(data) {
        var channelName = data.channel;
        if (!channelName) {
            return;
        }
        scProxy.unwatch(channelName);
        scProxy.unsubscribe(channelName);
    });
    bridge.registerHandler("emitEventHandler", function(data) {
        var eventName = data.event;
        scProxy.emit(eventName, data.data);
    });

    bridge.init(function(message, responseCallback) {
    });
});
