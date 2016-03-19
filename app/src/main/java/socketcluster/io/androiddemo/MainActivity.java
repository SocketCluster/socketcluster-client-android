package socketcluster.io.androiddemo;


/**
 * Dariusz Krempa
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fangjian.WebViewJavascriptBridge;

import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

import io.socketcluster.socketclusterandroidclient.SCSocketService;


public class MainActivity extends Activity {

    private static String TAG = "SCDemo";
    private SCSocketService scSocket;
    private Boolean bound = false;
    private String options;
    private boolean logout;
    private TextView textView;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check active subscriptions
        final Button subsBtn = (Button) findViewById(R.id.activeSubsBtn);
        textView = (TextView) findViewById(R.id.textView);
        subsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scSocket.subscriptions(true, new WebViewJavascriptBridge.WVJBResponseCallback() {
                    @Override
                    public void callback(String data) {
                        handleEvents("subscriptions", data);
                    }
                });
            }
        });
        Map map = new HashMap();

        String host = "ns1.diskstation.eu";
        String port = "3000";
        map.put("hostname", host);
        map.put("port", port);
        options = JSONValue.toJSONString(map);

        // Get connection state button
        final Button stateBtn = (Button) findViewById(R.id.stateBtn);
        stateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scSocket.getState(new WebViewJavascriptBridge.WVJBResponseCallback() {
                    @Override
                    public void callback(String data) {
                        handleEvents("getState", data);
                    }
                });
            }
        });
        // Benchmark
        final Button benchmark = (Button) findViewById(R.id.benchmark);
        benchmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scSocket.emitEvent("benchmark", "start");
            }
        });
        // Disconnect button
        final Button disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scSocket.disconnect();
                logout = true;
            }
        });
        // Login
        final Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scSocket.emitEvent("login", "test");
            }
        });
        // Subscribe to WEATHER channel
        final Button subToWeatherBtn = (Button) findViewById(R.id.subBtn);
        subToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String channel = "WEATHER";
                scSocket.subscribe(channel);
            }
        });
        //Unsubscribe WEATHER channel
        final Button unSubToWeatherBtn = (Button) findViewById(R.id.unsubsBtn);
        unSubToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String channel = "WEATHER";
                scSocket.unsubscribe(channel);
            }
        });
        // Publish to WEATHER channel
        final Button pubToWeatherBtn = (Button) findViewById(R.id.pubBtn);
        pubToWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String msg = "publish to channel";
                scSocket.publish("WEATHER", msg);
            }
        });
    }

    private ServiceConnection conn = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder){
            SCSocketService.SCSocketBinder scSocketBinder = (SCSocketService.SCSocketBinder) binder;
            scSocket = scSocketBinder.getBinder();
            scSocket.setDelegate(MainActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName component){
            bound = false;
        }
    };

    /**
     * Bind service is required to access methods in SCSocketService
     * startService is required, even if service is bound, to keep SCSocketCluster alive when activity isn't foreground app
     * this let to stay application connected to server and receive events and subscribed messages
     */
    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, SCSocketService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(bound){
            unbindService(conn);
            bound = false;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("io.socketcluster.eventsreceiver"));
    }

    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * BroadcastReceiver to receive messages from SCSocketClusterService to handle events
     * Broadcast receiver can be changed or even implemented at new class but has to be to handle events from socketcluster client
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");
            String data = intent.getStringExtra("data");
            handleEvents(event,data);
        }
    };


    public void handleEvents(String event, String data) {
        switch(event){

            default:
                textView.append(event + ": " + data + "\n");
                break;

            case SCSocketService.EVENT_ON_READY:
                scSocket.connect(options);
                textView.append(event + ": " + data + "\n");
                Log.d(TAG, "ready");
                break;

            case SCSocketService.EVENT_ON_CONNECT:
                scSocket.emitEvent("login", "Test Driver", new WebViewJavascriptBridge.WVJBResponseCallback() {
                    @Override
                    public void callback(String data) {
                        textView.append("callback: "+data+"\n");
                    }
                });

                scSocket.registerEvent("rand");
                textView.append(event+": "+data+"\n");
                Log.d(TAG, "connected: "+data);
                break;

            case SCSocketService.EVENT_ON_DISCONNECT:
                if(!logout)
                    scSocket.authenticate(authToken);

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "disconnected");
                break;

            case SCSocketService.EVENT_ON_EVENT_MESSAGE:
                textView.setText(event + ": " + data + "\n");

                Log.d(TAG, "onEvent: "+data);
                break;

            case SCSocketService.EVENT_ON_SUBSCRIBED_MESSAGE:

                textView.append(event + ": " + data + "\n");
                Log.d(TAG, "subscribed message: "+data);
                break;

            case SCSocketService.EVENT_ON_AUTHENTICATE_STATE_CHANGE:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "authStateChanged: "+data);
                break;

            case SCSocketService.EVENT_ON_SUBSCRIBE_STATE_CHANGE:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "subscribeStateChanged: "+data);
                break;

            case SCSocketService.EVENT_ON_ERROR:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "error: "+data);
                break;

            case SCSocketService.EVENT_ON_SUBSCRIBE_FAIL:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "subscribeFailed: "+data);
                break;

            case SCSocketService.EVENT_ON_AUTHENTICATE:
                authToken = data;
                textView.append(event+": "+data+"\n");
                Log.d(TAG, "authenticated: "+ authToken);
                break;

            case SCSocketService.EVENT_ON_DEAUTHENTICATE:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "error: "+data);
                break;

            case SCSocketService.EVENT_ON_SUBSCRIBE:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "error: "+data);
                break;

            case SCSocketService.EVENT_ON_UNSUBSCRIBE:

                textView.append(event+": "+data+"\n");
                Log.d(TAG, "error: "+data);
                break;

        }
    }

}