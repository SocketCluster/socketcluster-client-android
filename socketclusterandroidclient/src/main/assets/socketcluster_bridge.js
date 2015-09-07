var scProxy;
function connectWebViewJavascriptBridge(callback) {
    callback(window.WebViewJavascriptBridge3);
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
        scProxy.on('error', function(err) {
            var jsonText = JSON.stringify({'data': err.message});
            bridge.callHandler('onErrorHandler', jsonText, function(response) {
            });
        });
        scProxy.removeAllListeners('connect');
        scProxy.on('connect', function() {
            var jsonText = JSON.stringify({});
            bridge.callHandler('onConnectHandler', jsonText, function(response) {
            });
        });
        scProxy.removeAllListeners('disconnect');
        scProxy.on('disconnect', function() {
            bridge.callHandler('onDisconnectedHandler', JSON.stringify({}), function(response) {
            });
        });

        scProxy.removeAllListeners('kickOut');
        scProxy.on('kickOut', function(data) {
            bridge.callHandler('onKickoutHandler', JSON.stringify({'data': data}), function(response) {
            });
        });

        scProxy.removeAllListeners('subscribe');
        scProxy.on('subscribe', function(data) {
            bridge.callHandler('onSubscribeHandler', JSON.stringify({'data': data}), function(response) {
            });
        });

        scProxy.removeAllListeners('subscribeFail');
        scProxy.on('subscribeFail', function(data) {
            bridge.callHandler('onSubscribeFailHandler', JSON.stringify({'data': data}), function(response) {
            });
        });

        scProxy.removeAllListeners('unsubscribe');
        scProxy.on('unsubscribe', function(data) {
            bridge.callHandler('onUnsubscribeHandler', JSON.stringify({'data': data}), function(response) {
            });
        });
    });

    bridge.registerHandler("disconnectHandler", function(data) {
        scProxy.disconnect();
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
        packet = JSON.parse(packet);
        var channelName = packet.channel;
        if (!channelName) {
            return;
        }
        scProxy.publish(channelName, JSON.stringify({
            'data': packet.data
        }));
    });

    bridge.registerHandler("subscribeHandler", function(packet) {
        packet = JSON.parse(packet);
        var channelName = packet.channel;
        if (!channelName) {
            return;
        }

        if (!scProxy.isSubscribed(channelName)) {
            scProxy.watch(channelName, function(publishedData) {
                bridge.callHandler('onChannelReceivedEventFromSocketCluster', JSON.stringify({
                    'channel': channelName,
                    'data': JSON.stringify(publishedData)
                }));
            });
        }
        scProxy.subscribe(channelName);
    });
    bridge.registerHandler("unsubscribeHandler", function(data) {
        data = JSON.parse(data);
        var channelName = data.channel;
        if (!channelName) {
            return;
        }
        scProxy.unwatch(channelName);
        scProxy.unsubscribe(channelName);
    });
    bridge.registerHandler("emitEventHandler", function(data) {
        data = JSON.parse(data);
        var eventName = data.event;
        scProxy.emit(eventName, data.data);
    });

    bridge.init(function(message, responseCallback) {
    });

});


