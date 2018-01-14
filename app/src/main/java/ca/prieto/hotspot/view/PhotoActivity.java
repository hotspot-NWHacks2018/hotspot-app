package ca.prieto.hotspot.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import ca.prieto.hotspot.R;
import ca.prieto.hotspot.utils.ParsingUtils;
import ca.prieto.hotspot.utils.WifiUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

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

    private Button newImage;
    private ImageView capturedImage;
    EditText scannedText;
    String datapath;
    private TessBaseAPI mTess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        captureImageLayout = findViewById(R.id.take_picture_layout);
        loadingLayout = findViewById(R.id.loading_layout);
        connectToWifiLayout = findViewById(R.id.connect_to_wifi_layout);
        actionCard = findViewById(R.id.action_card);

        //actionCard.animate().translationYBy()

        showCaptureImage();

        cameraView = findViewById(R.id.cameraView);
        captureImage = findViewById(R.id.captureImage);
        //newImage = (Button) findViewById(R.id.newImage);
        capturedImage = findViewById(R.id.capturedImage);
        //scannedText = findViewById(R.id.networkName);
        datapath = getFilesDir()+ "/tesseract/";

        recaptureButton = findViewById(R.id.recaptureButton);
        connectButton = findViewById(R.id.connectButton);
        networkNameEditText = findViewById(R.id.NetworkName);
        passwordEditText = findViewById(R.id.Password);

        //capturedImage.setVisibility(View.GONE);
        //newImage.setVisibility(View.GONE);

        connectButton.setOnClickListener(v -> {
            WifiUtils.connectToWifi(this, networkNameEditText.getText().toString(), passwordEditText.getText().toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show(),
                               t -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
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

                //cameraView.setVisibility(View.GONE);
              //  captureImage.setVisibility(View.GONE);

//                capturedImage.setVisibility(View.VISIBLE);
  //              newImage.setVisibility(View.VISIBLE);
    //            scannedText.setVisibility(View.VISIBLE);
            }
        });

//        newImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(PhotoActivity.this, PhotoActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
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

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());

        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void copyFiles() {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
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

    private List<String> parseText(String text) {
        List<String> values;

        values = Arrays.asList(text.split("\\s+"));

        return values;
    }
}
