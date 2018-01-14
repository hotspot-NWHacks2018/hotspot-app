package ca.prieto.hotspot.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.prieto.hotspot.R;

public class MainActivity extends AppCompatActivity {
    Button photoConnect;
    Button hotspotConnect;
    Button hotspotSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoConnect = (Button) findViewById(R.id.photoConnect);
        hotspotConnect = (Button) findViewById(R.id.hotspotConnect);
        hotspotSetup = (Button) findViewById(R.id.hotspotSetup);

        photoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                startActivity(intent);
            }
        });

        hotspotConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HotspotConnectActivity.class);
                startActivity(intent);
            }
        });

        hotspotSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HotspotSetupActivity.class);
                startActivity(intent);
            }
        });
    }
}
