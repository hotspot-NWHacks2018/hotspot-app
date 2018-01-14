package ca.prieto.hotspot.utils;

/**
 * Created by Jessica on 2018-01-13.
 */

public class NfcUtils {

    public static void send(String message, SendCallback callback) {

    }

    public static void receive (ReceiveCallback callback) {
    }

    public interface SendCallback {
        void onSuccess();
        void onFailure();
    }

    public interface ReceiveCallback {
        void onSuccess(String message);
        void onFailure();
        void onConnected();
    }
}