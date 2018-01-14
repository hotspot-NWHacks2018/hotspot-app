package ca.prieto.hotspot.view;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

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

        showCaptureImage();

        cameraView = findViewById(R.id.cameraView);
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
                            Snackbar.make(actionCard, "Could not connect. Please verify the details above.", Snackbar.LENGTH_SHORT).show();
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

                CameraUtils.decodeBitmap(picture, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
//                        capturedImage.setImageBitmap(bitmap);

                        ParsingUtils.parseNetworkCredentials(PhotoActivity.this, bitmap)
                            .onErrorReturnItem(new ParsingUtils.NetworkCredentials("", ""))
                            .subscribe(creds -> {
                                showWifiDetails();
                                networkNameEditText.setText(creds.getSsid());
                                passwordEditText.setText(creds.getPassword());
                            });
                    }
                });
            }
        });

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoading();
                cameraView.captureSnapshot();
            }
        });
    }

    private void showCaptureImage() {
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
