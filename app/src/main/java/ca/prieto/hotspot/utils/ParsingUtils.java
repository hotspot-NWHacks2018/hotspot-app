package ca.prieto.hotspot.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by George on 2018-01-13.
 */

public class ParsingUtils {

    public static Observable<NetworkCredentials> parseNetworkCredentials(Context context, Bitmap image) {
        TessBaseAPI tessBaseAPI = TessUtils.getTestBaseApi(context);
        return Observable.just(tessBaseAPI)
                .observeOn(Schedulers.io())
                .doOnNext(api -> api.setImage(image))
                .map(api -> api.getUTF8Text())
                .map(ParsingUtils::parseStringToNetworkCreds)
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static NetworkCredentials parseStringToNetworkCreds(String rawNetworkCreds) {
        String[] tokenizedNetworkCreds = rawNetworkCreds.split("[!@#$%^&*():[\\\\]\\s]+");
        return new NetworkCredentials(tokenizedNetworkCreds[1], tokenizedNetworkCreds[3]);
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
