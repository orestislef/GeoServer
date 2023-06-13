package gr.orestislef.geoserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the KeepAliveService
        Intent intent = new Intent(this, KeepAliveService.class);
        startService(intent);
    }
}