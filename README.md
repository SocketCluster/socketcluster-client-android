Native Android client for SocketCluster

Please read MainActivity.java for existing usages, feedbacks are welcome.

Project on basis ilani project. Updated to SocketCluster client version 4.3. As for now android is able to handle most of events and methods from scSocket client.

Ive been trying to make it as simplest as i can to handle messages at activity. Refactoring existing code, which was very usefull and most of this client is based on forked code, is necessary to keep this client connected to server even if application is in background. To achieve this android client is working as service.

Implementing this client in application is very simple. Somwhere in app, probably in main activity, implement BroadcastReceiver:

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");
            String data = intent.getStringExtra("data");
            handleEvents(event,data); //this line is optional
        }
    };
And thats it. From this moment you can handle each event sent from server. Of course its also possible to implement BroadcastReceiver as new class.

Benchmark button. I wanted to check how efficient is android client, not very efficient but is dispatching all messages even if its many of them in short time. This is code i used in worker:

    socket.on('benchmark', function(data){
        for(n=0;n<500;n++){
            socket.emit('rand', {
                    rand: n
            });
            console.log('emit' + n);
        }
    });
