package ca.prieto.hotspot.view;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.thanosfisherman.wifiutils.WifiUtils;

import ca.prieto.hotspot.R;
import ca.prieto.hotspot.utils.ParsingUtils;

public class PhotoActivity extends AppCompatActivity {
    private View captureImageLayout;
    private View loadingLayout;
    private View connectToWifiLayout;
    private View actionCard;

    private CameraView cameraView;
    private Button captureImage;

    private Button recaptureButton;
    private Button connectButton;
    private EditText networkNameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();

        captureImageLayout = findViewById(R.id.take_picture_layout);
        loadingLayout = findViewById(R.id.loading_layout);
        connectToWifiLayout = findViewById(R.id.connect_to_wifi_layout);
        actionCard = findViewById(R.id.action_card);

        actionCard.setY(500);
        actionCard.animate().translationYBy(-500).setDuration(2).start();

        cameraView = findViewById(R.id.cameraView);
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        cameraView.mapGesture(Gesture.LONG_TAP, GestureAction.CAPTURE);

        captureImage = findViewById(R.id.captureImage);

        recaptureButton = findViewById(R.id.recaptureButton);
        connectButton = findViewById(R.id.connectButton);
        networkNameEditText = findViewById(R.id.NetworkName);
        passwordEditText = findViewById(R.id.Password);

        connectButton.setOnClickListener(v -> {
            WifiUtils.withContext(this)
                    .connectWith(networkNameEditText.getText().toString(), passwordEditText.getText().toString())
                    .onConnectionResult(isSuccess -> {
                        if (isSuccess) {
                            startActivity(new Intent(this, ConnectedActivity.class));
                            finish();
                        } else {
                            Snackbar.make(actionCard, "Cou ld not connect. Please verify the details above.", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .start();
        });

        recaptureButton.setOnClickListener(v -> {
            cameraView.start();
            showCaptureImage();
        });


        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                cameraView.stop();

                CameraUtils.decodeBitmap(picture, bitmap -> {
                    ParsingUtils.parseNetworkCredentials(PhotoActivity.this, bitmap)
                        .subscribe(creds -> {
                            showWifiDetails();
                            networkNameEditText.setText(creds.getSsid());
                            passwordEditText.setText(creds.getPassword());
                        });
                });
            }
        });

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoading();
                cameraView.capturePicture();
            }
        });

        showCaptureImage();
    }

    private void showCaptureImage() {
        cameraView.start();
        captureImageLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        connectToWifiLayout.setVisibility(View.GONE);
    }

    private void showLoading() {
        captureImageLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
        connectToWifiLayout.setVisibility(View.GONE);
    }

    private void showWifiDetails() {
        captureImageLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        connectToWifiLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (captureImageLayout.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        }
        showCaptureImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}
