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

  	/**
     * scSocket.connect handler
     */
    bridge.registerHandler("connectHandler", function(data) {
        var data = JSON.parse(data);
        scProxy = socketCluster.connect(data);

        scProxy.removeAllListeners('error');
        scProxy.on('error', function(err) {
            var jsonText = JSON.stringify({'data': err.message});
            bridge.callHandler('onErrorHandler', jsonText, function(response) {
            });
        });
        scProxy.removeAllListeners('connect');
        scProxy.on('connect', function(data) {
            var jsonText = JSON.stringify({'data':data});
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

        scProxy.removeAllListeners('authenticate');
        scProxy.on('authenticate', function(data) {
            bridge.callHandler('onAuthenticateHandler', JSON.stringify({'data':data}), function(response) {
            });
        });

        scProxy.removeAllListeners('deauthenticate');
        scProxy.on('deauthenticate', function(){
          bridge.callHandler('onDeauthenticateHandler', JSON.stringify({}), function(response) {
          });
        });

        scProxy.removeAllListeners('authStateChange');
        scProxy.on('authStateChange', function(data) {
          bridge.callHandler('onAuthStateChangeHandler', JSON.stringify ({'data':data}), function(response) {
          });
        });

        scProxy.removeAllListeners('subscribeStateChange');
        scProxy.on('subscribeStateChange', function(data) {
          bridge.callHandler('onSubscribeStateChangeHandler', JSON.stringify ({'data':data}), function(response) {
          });
        });

    });

    /**
     * scSocket.disconnect handler
     */
    bridge.registerHandler("disconnectHandler", function(data) {
        scProxy.disconnect();
    });

    /**
     *  scSocket.on(event) handler
     */
    bridge.registerHandler("onEventHandler", function(data, responseCallback) {
        data = JSON.parse(data);
        var eventName = data.event;
        scProxy.on(eventName, function(data, respCallback) {
            bridge.callHandler('onEventReceivedFromSocketCluster', JSON.stringify({
                'event': eventName,
                'data': JSON.stringify(data)
            }));
        });
    });

    /**
     *  scSocket.off(event) handler
     */
    bridge.registerHandler("offEventHandler", function(data) {
        data = JSON.parse(data);
        scProxy.off(data.event);
    });

    /**
     *  scSocket.publish handler
     */
    bridge.registerHandler("publishHandler", function(packet, responseCallback) {
        packet = JSON.parse(packet);
        var channelName = packet.channel;
        if (!channelName) {
            return;
        }
        scProxy.publish(
          channelName,
          JSON.stringify({'data': packet.data})
        );
    });

    /**
     *  scSocket.subscribe handler
     */
    bridge.registerHandler("subscribeHandler", function(packet) {
        packet = JSON.parse(packet);
        var channelName = packet.channel;
        if (!channelName || scProxy.state != scProxy.OPEN) {
            return;
        }

        if (!scProxy.isSubscribed(channelName, true)) {
            scProxy.watch(channelName, function(publishedData) {
                bridge.callHandler('onChannelReceivedEventFromSocketCluster', JSON.stringify({
                    'channel': channelName,
                    'data': JSON.stringify(publishedData)
                }));
            });
        }
        scProxy.subscribe(channelName);
    });

    /**
     *  scSocket.unsubscribe handler
     */
    bridge.registerHandler("unsubscribeHandler", function(data) {
        data = JSON.parse(data);
        var channelName = data.channel;
        if (!channelName) {
            return;
        }
        scProxy.unwatch(channelName);
        scProxy.unsubscribe(channelName);
    });

    /**
     *  scSocket.emit handler
     */
    bridge.registerHandler("emitEventHandler", function(data) {
        data = JSON.parse(data);
        scProxy.emit(data.event, data.data);
    });

    /**
     *  scSocket.deauthenticate handler
     */
    bridge.registerHandler("deauthenticateHandler", function() {
        scProxy.deauthenticate();
    });

    /**
     * scSocket.getState handler
     */
    bridge.registerHandler("getStateHandler", function() {
        var state = scProxy.getState();
        bridge.callHandler('onGetStateHandler', JSON.stringify(state));
    });

    /**
     * scSocket.subscriptions handler
     */
    bridge.registerHandler("subscriptionsHandler", function(includePending, callback) {
        var state = scProxy.subscriptions(includePending);
        callback(JSON.stringify(state));
    });
    /**
     *  scSocket.authenticate handler
     */
    bridge.registerHandler("authenticateHandler", function(data) {
        scProxy.authenticate(data);
    });

    bridge.init(function(message) {
    });

    bridge.callHandler('readyHandler');
});