package ca.prieto.hotspot.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by George on 2018-01-13.
 */

public class ParsingUtils {

    public static Observable<NetworkCredentials> parseNetworkCredentials(Context context, Bitmap image) {
        return Observable.just("")
                .observeOn(Schedulers.io())
                .map(__ -> TessUtils.getTestBaseApi(context))
                .doOnNext(api -> api.setImage(image))
                .map(api -> api.getUTF8Text())
                .onErrorReturnItem("")
                .map(ParsingUtils::parseStringToNetworkCreds)
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static NetworkCredentials parseStringToNetworkCreds(String rawNetworkCreds) {
        String[] tokenizedNetworkCreds = rawNetworkCreds.split("[!@#$%^&*():[\\\\]\\s]+");

        if (tokenizedNetworkCreds.length >= 4) {
            // most likely 2 of those words are labels for the ssid and pass
            return new NetworkCredentials(tokenizedNetworkCreds[1], tokenizedNetworkCreds[3]);
        } else if (tokenizedNetworkCreds.length == 2) {
            return new NetworkCredentials(tokenizedNetworkCreds[0], tokenizedNetworkCreds[1]);
        }
        // we tried but couldnt make anything good enough out
        return new NetworkCredentials("", "");
    }

    public static class NetworkCredentials {
        private String ssid;
        private String password;

        public NetworkCredentials(String ssid, String password) {
            this.ssid = ssid;
            this.password = password;
        }

        public String getSsid() {
            return ssid;
        }

        public String getPassword() {
            return password;
        }
    }
}
