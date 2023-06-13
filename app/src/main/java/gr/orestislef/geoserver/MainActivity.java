package gr.orestislef.geoserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver viewChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle the broadcast and change the view accordingly
            String lat = intent.getStringExtra("lat");
            String lng = intent.getStringExtra("lng");
            String response = intent.getStringExtra("response");
            changeView(lat,lng,response);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Register the BroadcastReceiver to receive the broadcast
        IntentFilter filter = new IntentFilter("com.example.ACTION_VIEW_CHANGE");
        registerReceiver(viewChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver when the activity is paused
        unregisterReceiver(viewChangeReceiver);
    }

    private void changeView(String lat, String lng, String response) {
        String latLng = "lat: "+lat + "lng: "+lng;
        requestTV.setText(latLng);

        responseTV.setText(response);
    }

    TextView requestTV, responseTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the KeepAliveService
        Intent intent = new Intent(this, KeepAliveService.class);
        startService(intent);

        TextView ipTV = findViewById(R.id.ipTV);
        String ipAndPort = getDeviceIPAddress() + ":" + ServerService.PORT;
        ipTV.setText(ipAndPort);

        requestTV = findViewById(R.id.requestTV);
        responseTV = findViewById(R.id.responseTV);
    }

    public String getDeviceIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        String ipAddress = address.getHostAddress();
                        // Check if it is an IPv4 address
                        boolean isIPv4 = ipAddress.indexOf(':') < 0;
                        if (isIPv4) {
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

