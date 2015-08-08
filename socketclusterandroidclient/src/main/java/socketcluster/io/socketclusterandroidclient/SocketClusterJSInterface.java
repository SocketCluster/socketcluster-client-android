package socketcluster.io.socketclusterandroidclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Created by lihanli on 8/06/2015.
 */
public class SocketClusterJSInterface {
    private Context con;

    public SocketClusterJSInterface(Context con) {
        this.con = con;
    }

    @android.webkit.JavascriptInterface
    public void showToast() {
        Log.e("JSInterface", "test");
    }
}
