package com.fangjian;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Created with IntelliJ IDEA.
 * User: jack_fang
 * Date: 13-8-15
 */
public class WebViewJavascriptBridge {

    WebView mWebView;
    Activity mContext;
    WVJBHandler _messageHandler;
    Map<String,WVJBHandler> _messageHandlers;
    Map<String,WVJBResponseCallback> _responseCallbacks;
    long _uniqueId;
    WVJBHandler handler;

    public WebViewJavascriptBridge(Activity context,WebView webview,WVJBHandler handler) {
        this.mContext=context;
        this.mWebView=webview;
        this._messageHandler=handler;
        _messageHandlers=new HashMap<String,WVJBHandler>();
        _responseCallbacks=new HashMap<String, WVJBResponseCallback>();
        _uniqueId=0;
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(this, "_WebViewJavascriptBridge");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());     //optional, for show console and alert
    }


//    private void loadWebViewJavascriptBridgeJs(WebView webView) {
//        InputStream is=mContext.getResources().openRawResource(R.raw.webviewjavascriptbridge);
//        String script=convertStreamToString(is);
//        webView.loadUrl("javascript:"+script);
//
//    }

    public static String convertStreamToString(InputStream is) {
        String s="";
        try{
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) s= scanner.next();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            Log.d("test", "onPageFinished");
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.d("console.log: ", cm.message()
                            +" line:"+ cm.lineNumber()
            );
            return true;
        }

    }


    public interface WVJBHandler{
        public void handle(String data, WVJBResponseCallback jsCallback);
    }

    public interface WVJBResponseCallback{
        public void callback(String data);
    }
    @JavascriptInterface
    public void registerHandler(String handlerName,WVJBHandler handler) {
        _messageHandlers.put(handlerName, handler);
    }

    private class CallbackJs implements WVJBResponseCallback{
        private final String callbackIdJs;

        public  CallbackJs(String callbackIdJs){
            this.callbackIdJs=callbackIdJs;
        }
        @Override
        public void callback(String data) {
            _callbackJs(callbackIdJs,data);
        }
    }

    @JavascriptInterface
    private void _callbackJs(String callbackIdJs,String data) {
        //TODO: CALL js to call back;
        Map<String,String> message=new HashMap<String, String>();
        message.put("responseId",callbackIdJs);
        message.put("responseData",data);
        _dispatchMessage(message);
    }

    @JavascriptInterface
    public void _handleMessageFromJs(final String data,String responseId,
                                     String responseData,String callbackId,String handlerName){

        if (null!=responseId) {
            final WVJBResponseCallback responseCallback = _responseCallbacks.get(responseId);
            responseCallback.callback(responseData);
            _responseCallbacks.remove(responseId);
        } else {
            final WVJBResponseCallback responseCallback;
            if (null!=callbackId) {
                responseCallback = new CallbackJs(callbackId);
            } else {
                responseCallback = null;
            }

            if (null!=handlerName) {
                handler = _messageHandlers.get(handlerName);
                if (null==handler) {
                    Log.e("test","WVJB Warning: No handler for "+handlerName);
                    return ;
                }
            } else {
                handler = _messageHandler;
            }
            try {
                handler.handle(data, responseCallback);
            }catch (Exception exception) {
                Log.e("test","WebViewJavascriptBridge: WARNING: java handler threw. "+exception.getMessage());
            }
        }
    }
    @JavascriptInterface
    public void send(String data) {
        send(data,null);
    }
    @JavascriptInterface
    public void send(String data ,WVJBResponseCallback responseCallback) {
        _sendData(data,responseCallback,null);
    }
    @JavascriptInterface
    private void _sendData(String data,WVJBResponseCallback responseCallback,String  handlerName){
        Map <String, String> message=new HashMap<String,String>();
        message.put("data",data);
        if (null!=responseCallback) {
            String callbackId = "java_cb_"+ (++_uniqueId);
            _responseCallbacks.put(callbackId,responseCallback);
            message.put("callbackId",callbackId);
        }
        if (null!=handlerName) {
            message.put("handlerName", handlerName);
        }
        _dispatchMessage(message);
    }
    @JavascriptInterface
    private void _dispatchMessage(Map <String, String> message){
        String messageJSON = new JSONObject(message).toString();
        Log.d("_dispatchMessageWVJB","sending:"+messageJSON);
        final  String javascriptCommand =
                String.format("javascript:WebViewJavascriptBridge3._handleMessageFromJava('%s');",doubleEscapeString(messageJSON));
        mContext.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                mWebView.loadUrl(javascriptCommand);
            }
        });
    }
    @JavascriptInterface
    public  void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }
    @JavascriptInterface
    public void callHandler(String handlerName,String data) {
        callHandler(handlerName, data, null);
    }
    @JavascriptInterface
    public void callHandler(String handlerName,String data,WVJBResponseCallback responseCallback){
        _sendData(data, responseCallback,handlerName);
    }

    /*
      * you must escape the char \ and  char ", or you will not recevie a correct json object in 
      * your javascript which will cause a exception in chrome.
      *
      * please check this and you will know why.
      * http://stackoverflow.com/questions/5569794/escape-nsstring-for-javascript-input
      * http://www.json.org/
    */
    private String doubleEscapeString(String javascript) {
        String result;
        result = javascript.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        result = result.replace("\'", "\\\'");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\f", "\\f");
        return result;
    }

}