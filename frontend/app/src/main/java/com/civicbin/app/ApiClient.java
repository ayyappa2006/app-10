package com.civicbin.app;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {
    // 10.0.2.2 is the alias to host loopback (127.0.0.1) on Android Emulator
    public static final String EMULATOR_URL = "http://10.0.2.2/civic_backend/";
    // Host Wi-Fi LAN IP for physical Android devices connected to the same network
    public static final String PHYSICAL_DEVICE_URL = "http://10.247.210.67/civic_backend/";

    private static String customBaseUrl = null;

    public static boolean isEmulator() {
        return (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.PRODUCT.contains("sdk_google")
                || android.os.Build.PRODUCT.contains("google_sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk_x86")
                || android.os.Build.PRODUCT.contains("sdk_gphone")
                || android.os.Build.PRODUCT.contains("vbox86p")
                || android.os.Build.PRODUCT.contains("emulator")
                || android.os.Build.PRODUCT.contains("simulator");
    }

    public static String getBaseUrl() {
        if (customBaseUrl != null && !customBaseUrl.isEmpty()) {
            return customBaseUrl.endsWith("/") ? customBaseUrl : (customBaseUrl + "/");
        }
        return isEmulator() ? EMULATOR_URL : PHYSICAL_DEVICE_URL;
    }

    public static void setBaseUrl(String url) {
        customBaseUrl = url;
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public static void post(String endpoint, JSONObject jsonBody, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(getBaseUrl() + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);           
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    mainHandler.post(() -> callback.onSuccess(jsonResponse));
                } else {
                    mainHandler.post(() -> callback.onError("Server returned code: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + (e.getMessage() != null ? e.getMessage() : "Connection failed")));
            }
        });
    }

    public static void get(String endpoint, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(getBaseUrl() + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    mainHandler.post(() -> callback.onSuccess(jsonResponse));
                } else {
                    mainHandler.post(() -> callback.onError("Server returned code: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + (e.getMessage() != null ? e.getMessage() : "Connection failed")));
            }
        });
    }

    public static void uploadPhoto(String uriString, android.content.ContentResolver resolver, ApiCallback callback) {
        executor.execute(() -> {
            try {
                android.net.Uri fileUri = android.net.Uri.parse(uriString);
                java.io.InputStream inputStream = resolver.openInputStream(fileUri);
                if (inputStream == null) {
                    mainHandler.post(() -> callback.onError("File not found"));
                    return;
                }

                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                java.net.URL url = new java.net.URL(getBaseUrl() + "upload_image.php");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("User-Agent", "Android Multipart Agent");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                java.io.DataOutputStream dos = new java.io.DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"uploaded_photo.jpg\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                dos.writeBytes(lineEnd);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                inputStream.close();
                dos.flush();
                dos.close();

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    mainHandler.post(() -> callback.onSuccess(jsonResponse));
                } else {
                    mainHandler.post(() -> callback.onError("Server returned code: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Upload error: " + e.getMessage()));
            }
        });
    }

    public static void loadImage(android.widget.ImageView imageView, String uriString, android.content.ContentResolver resolver) {
        if (uriString == null || uriString.isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }

        if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
            executor.execute(() -> {
                try {
                    String targetUrl = uriString;
                    if (uriString.contains("localhost") || uriString.contains("10.0.2.2") || uriString.contains("10.185.54.67") || uriString.contains("10.247.210.67")) {
                        try {
                            java.net.URL baseUrl = new java.net.URL(getBaseUrl());
                            String hostIp = baseUrl.getHost();
                            targetUrl = uriString.replace("localhost", hostIp)
                                    .replace("10.0.2.2", hostIp)
                                    .replace("10.185.54.67", hostIp)
                                    .replace("10.247.210.67", hostIp);
                        } catch (Exception ignored) {}
                    }
                    
                    java.net.URL url = new java.net.URL(targetUrl);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    java.io.InputStream input = connection.getInputStream();
                    android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeStream(input);
                    mainHandler.post(() -> imageView.setImageBitmap(myBitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> imageView.setImageDrawable(null));
                }
            });
        } else {
            try {
                java.io.InputStream is = resolver.openInputStream(android.net.Uri.parse(uriString));
                if (is != null) {
                    android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeStream(is);
                    imageView.setImageBitmap(myBitmap);
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageDrawable(null);
            }
        }
    }
}
