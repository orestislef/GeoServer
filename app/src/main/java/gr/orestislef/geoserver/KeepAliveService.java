package gr.orestislef.geoserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepAliveService extends Service {

    private ServerService serverService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serverService = new ServerService(this);
        serverService.start();

        // Return START_STICKY to ensure the service keeps running even if killed by the system
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serverService != null) {
            serverService.stopServer();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}