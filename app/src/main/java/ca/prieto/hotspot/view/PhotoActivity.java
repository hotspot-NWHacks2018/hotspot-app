package ca.prieto.hotspot.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ca.prieto.hotspot.R;

public class PhotoActivity extends AppCompatActivity {
    final static String photoActivity = "PHOTO_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
    }
}
