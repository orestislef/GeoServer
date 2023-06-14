package gr.orestislef.geoserver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private enum SERVICE_COMMANDS {
        START, STOP
    }

    public static final String LAT_KEY = "lat";
    public static final String LNG_KEY = "lng";
    public static final String MAX_RESULTS = "max";
    public static final String RESPONSE_KEY = "response";

    private final BroadcastReceiver viewChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle the broadcast and change the view accordingly
            String lat = intent.getStringExtra(LAT_KEY);
            String lng = intent.getStringExtra(LNG_KEY);
            String max = intent.getStringExtra(MAX_RESULTS);
            String response = intent.getStringExtra(RESPONSE_KEY);
            changeView(lat, lng, max, response);
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

    private void changeView(String lat, String lng, String max, String response) {
        String latLng = "lat: " + lat + " lng: " + lng + " maxResults: " + max;
        requestTV.setText(latLng);

        responseTV.setText(response);
    }

    TextView requestTV, responseTV, geocoderPresentTV, serviceTV;
    ImageView geocoderPresentIV, serviceIV;
    Button serviceOnOffBTN;

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


        geocoderPresentTV = findViewById(R.id.geocoder_presentTV);
        serviceTV = findViewById(R.id.serviceTV);
        geocoderPresentIV = findViewById(R.id.geocoder_presentIV);
        serviceIV = findViewById(R.id.serviceIV);

        serviceOnOffBTN = findViewById(R.id.service_on_offBTN);

        setUpGeocoderStatusUI();
        setUpServiceStatusUI();

        serviceOnOffBTN.setOnClickListener(this::OnClickedServiceOnOff);
    }

    private void OnClickedServiceOnOff(View view) {
        if (view.getTag() == SERVICE_COMMANDS.START) {
            Intent serviceIntent = new Intent(MainActivity.this, KeepAliveService.class);
            MainActivity.this.startService(serviceIntent);
        } else if (view.getTag() == SERVICE_COMMANDS.STOP) {
            Intent serviceIntent = new Intent(MainActivity.this, KeepAliveService.class);
            MainActivity.this.stopService(serviceIntent);
        }

        setUpServiceStatusUI();
    }

    private void setUpGeocoderStatusUI() {
        if (Geocoder.isPresent()) {
            geocoderPresentIV.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.green_circle));
            geocoderPresentTV.setText("Geocoder is Present");
        } else {
            geocoderPresentIV.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.red_circle));
            geocoderPresentTV.setText("Geocoder is NOT Present");
        }
    }

    private void setUpServiceStatusUI() {
        boolean isRunning = isServiceRunning(this, KeepAliveService.class);
        if (isRunning) {
            serviceIV.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.green_circle));
            serviceTV.setText("Server is Running");
            serviceOnOffBTN.setText("Stop Server");
            serviceOnOffBTN.setTag(SERVICE_COMMANDS.STOP);
        } else {
            serviceIV.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.red_circle));
            serviceTV.setText("Server is off");
            serviceOnOffBTN.setText("Start Server");
            serviceOnOffBTN.setTag(SERVICE_COMMANDS.START);
        }
    }

    public boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    // The service is running
                    return true;
                }
            }
        }
        // The service is not running
        return false;
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

