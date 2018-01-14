package ca.prieto.hotspot.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by George on 2018-01-13.
 */

public class TessUtils {

    public static TessBaseAPI getTestBaseApi(Context context) {
        checkFile(context, new File(context.getFilesDir() + "/tesseract/tessdata/"));
        TessBaseAPI testBaseApi = new TessBaseAPI();
        testBaseApi.init(context.getFilesDir() + "/tesseract", "eng");
        return testBaseApi;
    }

    private static void checkFile(Context context, File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles(context);
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = context.getFilesDir() + "/tesseract/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(context);
            }
        }
    }

    private static void copyFiles(Context context) {
        try {
            //location we want the file to be at
            String filepath = context.getFilesDir() + "/tesseract/tessdata/eng.traineddata";

            //get access to AssetManager
            AssetManager assetManager = context.getApplicationContext().getAssets();

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
}
