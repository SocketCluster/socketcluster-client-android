package socketcluster.io.socketclusterandroidclient;

/**
 * Created by lihanli on 9/08/2015.
 */
public interface ISocketCluster {
    void socketClusterReceivedEvent(String name, String data);
    void socketClusterChannelReceivedEvent(String name, String data);

    void socketClusterDidConnect();
    void socketClusterDidDisconnect();
    void socketClusterOnError(String error);
    void socketClusterOnKickOut();
    void socketClusterOnSubscribe();
    void socketClusterOnSubscribeFail();
    void socketClusterOnUnsubscribe();
}
