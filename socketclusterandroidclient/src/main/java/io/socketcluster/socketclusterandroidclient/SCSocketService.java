package io.socketcluster.socketclusterandroidclient;

/**
 * basis on ilani project
 * Dariusz Krempa
 */

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fangjian.WebViewJavascriptBridge;

import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;


public class SCSocketService extends Service {

    public static final String EVENT_ON_READY = "ready";
    public static final String EVENT_ON_CONNECT = "onConnect";
    public static final String EVENT_ON_DISCONNECT = "onDisconnect";
    public static final String EVENT_ON_EVENT_MESSAGE = "onEvent";
    public static final String EVENT_ON_SUBSCRIBED_MESSAGE = "onMessage";
    public static final String EVENT_ON_AUTHENTICATE_STATE_CHANGE = "onAuthStateChange";
    public static final String EVENT_ON_SUBSCRIBE_STATE_CHANGE = "onSubscribeStateChange";
    public static final String EVENT_ON_ERROR = "onError";
    public static final String EVENT_ON_SUBSCRIBE_FAIL = "onSubscribeFail";
    public static final String EVENT_ON_AUTHENTICATE = "onAuthenticate";
    public static final String EVENT_ON_DEAUTHENTICATE = "onDeauthenticate";
    public static final String EVENT_ON_SUBSCRIBE = "onSubscribe";
    public static final String EVENT_ON_UNSUBSCRIBE = "onUnsubscribe";
    public static final String EVENT_ON_KICKOUT = "onKickOut";

    private WebView webView;
    private WebViewJavascriptBridge bridge;
    private final String TAG = "SCClient";
    private Activity mContext;
    private final IBinder binder = new SCSocketBinder();

    public class SCSocketBinder extends Binder {
        public SCSocketService getBinder(){
            return SCSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }
    @Override
    public void onCreate(){
    }

    public void setDelegate(Context delegate) {
        if(webView == null) {
            this.mContext = (Activity) delegate;
            this.setupSCWebClient(mContext);
            this.registerHandles();
        }
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
            Log.d("UserServerHandler","Received message from javascript: "+ data);
            if (null !=jsCallback) {
                jsCallback.callback("Java said:Right back atcha");
            }
        }
    }


    private void setupSCWebClient(Activity context) {
        webView = new WebView(context);
        bridge = new WebViewJavascriptBridge(context, webView, new UserServerHandler());
        webView.setWebViewClient(
                new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }
                });
        webView.loadUrl("file:///android_asset/user_client.html");
    }

    private void registerHandles(){

        bridge.registerHandler("readyHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_READY, data);
            }
        });

        /**
         *  'connect' event handler
         *  @param data json object
         */
        bridge.registerHandler("onConnectHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_CONNECT, data);
            }
        });

        /**
         *  'disconnect' event handler
         */
        bridge.registerHandler("onDisconnectedHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_DISCONNECT, data);
            }
        });

        /**
         *  'error' event handler
         */
        bridge.registerHandler("onErrorHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_ERROR, data);
            }
        });

        /**
         *  'kickOut' event handler
         *  @param data - channel
         */
        bridge.registerHandler("onKickoutHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_KICKOUT, data);
            }
        });

        /**
         *  'subscribeFail' event handler
         *  @param data - error
         */
        bridge.registerHandler("onSubscribeFailHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_SUBSCRIBE_FAIL, data);
            }
        });

        /**
         *  'authenticate' event handler
         *  @param data - authToken
         */
        bridge.registerHandler("onAuthenticateHandler", new WebViewJavascriptBridge.WVJBHandler(){

            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_AUTHENTICATE, data);
            }
        });

        /**
         *  'deauthenticate' event handler
         */
        bridge.registerHandler("onDeauthenticateHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_DEAUTHENTICATE, data);
            }
        });

        /**
         *  'unsubscribe' event handler
         *  @param data channel name
         */
        bridge.registerHandler("onUnsubscribeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_UNSUBSCRIBE, data);
            }
        });

        /**
         *  'authChangeState' event handler
         */
        bridge.registerHandler("onAuthStateChangeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_AUTHENTICATE_STATE_CHANGE, data);
            }
        });

        /**
         *  'subscribeStateChange' event handler
         */
        bridge.registerHandler("onSubscribeStateChangeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_SUBSCRIBE_STATE_CHANGE, data);
            }
        });

        /**
         *  'subscribe' event handler
         */
        bridge.registerHandler("onSubscribeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_SUBSCRIBE, data);
            }
        });

        /**
         *  'on(event)' event handler
         */
        bridge.registerHandler("onEventReceivedFromSocketCluster", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_EVENT_MESSAGE, data);
            }
        });

        /**
         *  'channel' received data helper
         */
        bridge.registerHandler("onChannelReceivedEventFromSocketCluster", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                sendMsg(EVENT_ON_SUBSCRIBED_MESSAGE, data);
            }
        });
    }

    private void callJavaScript(WebView view, String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        String separator = "";
        for (Object param : params) {
            stringBuilder.append(separator);
            separator = ",";
            if(param instanceof String){
                stringBuilder.append("'");
            }
            stringBuilder.append(param);
            if(param instanceof String) {
                stringBuilder.append("'");
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        Log.i(TAG, "callJavaScript: call=" + call);

        view.loadUrl(call);
    }

    /**
     *  Call scSocket.connect
     *  @param options
     */
    public void connect(String options) {
        bridge.callHandler("connectHandler", options);
    }

    /**
     * Call scSocket.disconnect
     */
    public void disconnect() {
        bridge.callHandler("disconnectHandler");
    }

    /**
     *  Call scSocket.emit
     *  @param eventName
     *  @param eventData
     */
    public void emitEvent(String eventName, String eventData) {
        if (null == eventData) {
            eventData = "";
        }
        Map data = new HashMap();
        data.put("event", eventName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("emitEventHandler", jsonText);
    }

    public void emitEvent(String eventName, String eventData, WebViewJavascriptBridge.WVJBResponseCallback callback) {
        if (null == eventData) {
            eventData = "";
        }
        Map data = new HashMap();
        data.put("event", eventName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("emitEventCallbackHandler", jsonText, callback);
    }

    /**
     *  Call scSocket.on(event)
     */
    public void registerEvent(String eventName) {
        Map data = new HashMap();
        data.put("event", eventName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("onEventHandler", jsonText);
    }

    /**
     *  Call scSocket.off(event)
     *  @param eventName
     */
    public void unregisterEvent(String eventName) {
        Map data = new HashMap();
        data.put("event", eventName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("offEventHandler", jsonText);
    }

    /**
     *  Call scSocket.publish 
     *  @param channelName
     *  @param eventData
     */
    public void publish(String channelName, String eventData) {
        Map data = new HashMap();
        data.put("channel", channelName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("publishHandler", jsonText);
    }

    public void publish(String channelName, String eventData, WebViewJavascriptBridge.WVJBResponseCallback callback) {
        Map data = new HashMap();
        data.put("channel", channelName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("publishCallbackHandler", jsonText, callback);
    }

    /**
     *  Call scSocket.subscribe
     *  @param channelName
     */
    public void subscribe(String channelName) {
        Map data = new HashMap();
        data.put("channel", channelName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("subscribeHandler", jsonText);
    }

    /**
     *  Call scSocket.unsubscribe
     *  @param channelName
     */
    public void unsubscribe(String channelName) {
        Map data = new HashMap();
        data.put("channel", channelName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("unsubscribeHandler", jsonText);
    }

    /**
     *  Call scSocket.authenticate
     *  @param authToken
     */
    public void authenticate(String authToken) {
        bridge.callHandler("authenticateHandler", authToken);
    }

    public void authenticate(String authToken, WebViewJavascriptBridge.WVJBResponseCallback callback) {
        bridge.callHandler("authenticateCallbackHandler", authToken, callback);
    }

    /**
     *  Call scSocket.deauthenticate
     */
    public void deauthenticate() {
        bridge.callHandler("deauthenticateHandler");
    }

    public void deauthenticate(WebViewJavascriptBridge.WVJBResponseCallback callback) {
        bridge.callHandler("deauthenticateCallbackHandler", "", callback);
    }

    /**
     * Call scSocket.getState
     */
    public void getState(WebViewJavascriptBridge.WVJBResponseCallback callback){
        bridge.callHandler("getStateHandler", "", callback);
    }

    /**
     *  Call scSocket.subscriptions
     *  @param pending
     */
    public void subscriptions(Boolean pending, WebViewJavascriptBridge.WVJBResponseCallback callback){
        Map map = new HashMap();
        map.put("pending", pending.toString());
        String jsonText = JSONValue.toJSONString(map);
        bridge.callHandler("subscriptionsHandler", jsonText, callback);
    }

    private void sendMsg(String event, String data){

        /**
         * Sending events to handle at activity or any other broadcast receiver and its required as is
         */
        Intent intent = new Intent("io.socketcluster.eventsreceiver");
        intent.putExtra("event", event);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        /** TODO
         * This is only example how to handle events with notifications and can be removed

        final int NOTIFICATION_ID = 1;
        NotificationManager mNotificationManager;
        NotificationCompat.Builder builder;

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent myApp = new Intent(getApplicationContext(), mContext.getClass());
        myApp.setAction(Intent.ACTION_MAIN);
        myApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, myApp, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("SCSocketCluster")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(data))
                        .setContentText(event)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());*/
    }

}