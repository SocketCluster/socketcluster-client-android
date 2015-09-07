package socketcluster.io.socketclusterandroidclient;
import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.fangjian.WebViewJavascriptBridge;

import org.json.simple.JSONValue;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;


public class SocketCluster {
    private String host;
    private String port;
    private Boolean isHttps;
    private WebView webView;
    private WebViewJavascriptBridge bridge;
    private ISocketCluster socketClusterDelegate;
    private final String TAG = "SCClient";

    public SocketCluster(String host, String port, boolean isHttps, Activity context) {
        this.host = host;
        this.port = port;
        this.isHttps = isHttps;
        this.setupSCWebClient(context);
        this.registerHandles();
    }

    public void setDelegate(ISocketCluster delegate) {
        socketClusterDelegate = delegate;
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
            Log.d("test","Received message from javascript: "+ data);
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
        InputStream is=context.getResources().openRawResource(R.raw.user_client);
        String user_client_html=WebViewJavascriptBridge.convertStreamToString(is);
        webView.loadDataWithBaseURL("file:///android_asset/", user_client_html, "text/html", "utf-8", "");

//        InputStream is2 = context.getResources().openRawResource(R.raw.webviewjavascriptbridge);
//        String script= WebViewJavascriptBridge.convertStreamToString(is2);
//        webView.loadUrl("javascript:" + script);

    }
    private void registerHandles(){

        bridge.registerHandler("onConnectHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterDidConnect();
            }
        });


        bridge.registerHandler("onDisconnectedHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterDidConnect();
            }
        });


        bridge.registerHandler("onErrorHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnError(data);
            }
        });


        bridge.registerHandler("onKickoutHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnKickOut();
            }
        });


        bridge.registerHandler("onSubscribeFailHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnSubscribeFail();
            }
        });


        bridge.registerHandler("onUnsubscribeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnUnsubscribe();
            }
        });


        bridge.registerHandler("onSubscribeHandler", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                socketClusterDelegate.socketClusterOnSubscribe();
            }
        });

        bridge.registerHandler("onEventReceivedFromSocketCluster", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                HashMap<String, String> receivedData = (HashMap)JSONValue.parse(data);
                socketClusterDelegate.socketClusterReceivedEvent(receivedData.get("event"), receivedData.get("data"));
            }
        });
        bridge.registerHandler("onChannelReceivedEventFromSocketCluster", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                HashMap<String, String> receivedData = (HashMap) JSONValue.parse(data);
                socketClusterDelegate.socketClusterReceivedEvent(receivedData.get("channel"), receivedData.get("data"));
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



    public void connect() {
        Map map = new HashMap();
        map.put("hostname", host);
        map.put("secure", isHttps ? "true" : "false");
        map.put("port", port);
        String jsonText = JSONValue.toJSONString(map);
        bridge.callHandler("connectHandler", jsonText);
    }
    public void disconnect() {
        bridge.callHandler("disconnectHandler");
    }

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

    public void registerEvent(String eventName) {
        Map data = new HashMap();
        data.put("event", eventName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("onEventHandler", jsonText);
    }

    public void publishToChannel(String channelName, String eventData) {
        Map data = new HashMap();
        data.put("channel", channelName);
        data.put("data", eventData);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("publishHandler", jsonText);
    }

    public void subscribeToChannel(String channelName) {
        Map data = new HashMap();
        data.put("channel", channelName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("subscribeHandler", jsonText);
    }

    public void unsubscribeFromChannel(String channelName) {
        Map data = new HashMap();
        data.put("channel", channelName);
        String jsonText = JSONValue.toJSONString(data);
        bridge.callHandler("unsubscribeHandler", jsonText);
    }
}
