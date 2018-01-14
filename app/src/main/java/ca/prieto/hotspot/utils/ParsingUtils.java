package ca.prieto.hotspot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
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
        String[] tokenizedNetworkCreds = rawNetworkCreds.split("[:\\s]+");
        int noiseLimit = 7;
        Log.d("TokenizedData", Arrays.toString(tokenizedNetworkCreds));

        List<String> cleansedTokens = new ArrayList<>();
        for (String token: tokenizedNetworkCreds) {
            if (token.length() >= 3) {
                cleansedTokens.add(token);
            } else if (tokenizedNetworkCreds.length < noiseLimit && !cleansedTokens.isEmpty()) {
                int size = cleansedTokens.size();
                cleansedTokens.set(size - 1, cleansedTokens.get(size - 1) + token);
            }
        }

        Log.d("CleansedData", Arrays.toString(cleansedTokens.toArray()));

        if (cleansedTokens.size() >= 4) {
            // most likely 2 of those words are labels for the ssid and pass
            return new NetworkCredentials(cleansedTokens.get(1), cleansedTokens.get(3));
        } else if (cleansedTokens.size() == 2) {
            // we're going to assume the first item is the ssid and the next is the password
            return new NetworkCredentials(cleansedTokens.get(0), cleansedTokens.get(1));
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
