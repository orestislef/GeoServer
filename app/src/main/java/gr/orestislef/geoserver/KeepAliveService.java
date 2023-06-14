package gr.orestislef.geoserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class KeepAliveService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ServerChannel";
    private static final String CHANNEL_NAME = "Server Service Channel";
    private ServerService serverService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NOTIFICATION_ID, createNotification(this));
        serverService = new ServerService(this);
        serverService.start();

        // Return START_STICKY to ensure the service keeps running even if killed by the system
        return START_STICKY;
    }

    private Notification createNotification(Context context) {
        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Create a pending intent for the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Server Service")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent);

        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
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