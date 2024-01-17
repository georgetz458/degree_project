package com.example.weatheranalysis.classes.util;

import android.annotation.SuppressLint;

import com.google.firebase.auth.FirebaseAuth;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class UploadUtils {
    private UploadUtils(){

    }
    //dev host
    //public static final String HostURL = "https://10.0.2.2:8443";
    //production host
    public static final String HostURL = "https://weather-analysis-backend.azurewebsites.net";
    public static final String CollectURL = HostURL +"/collect";

    public static final String AppId = "aa335156d8dca97c82d8fb49238347dc";

    //χρήση μόνο για dev, σε production δεν χρησιμοποιείται η παρακάτω μέθοδος
    /**
     * Επιτρέπει https connections χωρίς να έχει έγκυρο SSL
     * Μόνο σε Localhost σε dev, επενεργοποιημένο σε production λόγω ασφάλειας
     */
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
    public static boolean isSingedIn(FirebaseAuth mAuth){

        return mAuth.getCurrentUser() != null;
    }
    public static void logOut(FirebaseAuth mAuth){

        mAuth.signOut();
    }

}
