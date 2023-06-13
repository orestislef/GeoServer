package gr.orestislef.geoserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

