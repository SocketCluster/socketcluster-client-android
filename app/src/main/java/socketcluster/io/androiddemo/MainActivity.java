package socketcluster.io.androiddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import socketcluster.io.socketclusterandroidclient.ISocketCluster;
import socketcluster.io.socketclusterandroidclient.SocketCluster;


public class MainActivity extends AppCompatActivity implements ISocketCluster {

    private SocketCluster sc;
    private static String TAG = "SCDemo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sc = new SocketCluster("192.168.199.103", "8000", false, this);
        sc.setDelegate(this);
        // Connect button
        final Button connectBtn = (Button) findViewById(R.id.btnConnect);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sc.connect();
            }
        });
        // Disconnect button
        final Button disconnectBtn = (Button) findViewById(R.id.btnDisconnect);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sc.disconnect();
            }
        });
        // Listen to Rand event button handler
        final Button listenToRandBtn = (Button) findViewById(R.id.btnListenRand);
        listenToRandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sc.registerEvent("rand");
            }
        });
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

    @Override
    public void socketClusterReceivedEvent(String name, String data) {
        Log.i(TAG, "ReceivedEvent " + name);
        Log.i(TAG, "ReceivedEvent " + data);
    }

    @Override
    public void socketClusterChannelReceivedEvent(String name, String data) {
        Log.i(TAG, "socketClusterChannelReceivedEvent " + name + " data: " + data);
    }
    @Override
    public void socketClusterDidConnect() {
        Log.i(TAG, "SocketClusterDidConnect");
    }
    @Override
    public void socketClusterDidDisconnect() {
        Log.i(TAG, "socketClusterDidDisconnect");
    }
    @Override
    public void socketClusterOnError(String error) {
        Log.i(TAG, "socketClusterOnError");
    }
    @Override
    public void socketClusterOnKickOut() {
        Log.i(TAG, "socketClusterOnKickOut");
    }
    @Override
    public void socketClusterOnSubscribe() {
        Log.i(TAG, "socketClusterOnSubscribe");
    }
    @Override
    public void socketClusterOnSubscribeFail() {
        Log.i(TAG, "socketClusterOnSubscribeFail");
    }
    @Override
    public void socketClusterOnUnsubscribe() {
        Log.i(TAG, "socketClusterOnUnsubscribe");
    }
}
