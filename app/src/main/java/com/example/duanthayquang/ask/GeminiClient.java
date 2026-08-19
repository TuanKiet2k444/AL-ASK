package com.example.duanthayquang.ask;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiClient {

    private static final String TAG = "GeminiClient";
    private static final String MODEL = "gemini-3.5-flash";

    public static String ask(String apiKey, String prompt, String imageBase64, String mimeType) throws Exception {
        Log.d(TAG, "request start model=" + MODEL + " promptLength=" + prompt.length() + " hasImage=" + (imageBase64 != null));
        String urlString = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + apiKey;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        JSONArray parts = new JSONArray();
        
        // Add text part
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);

        // Add image part if exists
        if (imageBase64 != null && mimeType != null) {
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", imageBase64);
            imagePart.put("inline_data", inlineData);
            parts.put(imagePart);
        }

        JSONObject content = new JSONObject();
        content.put("parts", parts);

        JSONObject body = new JSONObject();
        body.put("contents", new JSONArray().put(content));

        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        Log.d(TAG, "request body bytes=" + bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }

        int code = conn.getResponseCode();
        Log.d(TAG, "response httpCode=" + code);
        BufferedReader reader;
        if (code >= 200 && code < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();

        String responseBody = sb.toString();
        if (code < 200 || code >= 300) {
            Log.e(TAG, "request failed httpCode=" + code + " body=" + responseBody);
            String message = "Server Error " + code;
            try {
                JSONObject errorJson = new JSONObject(responseBody);
                if (errorJson.has("error")) {
                    message = errorJson.getJSONObject("error").getString("message");
                }
            } catch (Exception ignored) {}
            throw new Exception(message);
        }

        Log.d(TAG, "response body length=" + responseBody.length());
        JSONObject json = new JSONObject(responseBody);
        
        if (!json.has("candidates") || json.getJSONArray("candidates").length() == 0) {
            if (json.has("promptFeedback")) {
                throw new Exception("Safety filter blocked the response.");
            }
            throw new Exception("AI returned no results.");
        }

        String text = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
        Log.d(TAG, "parsed text length=" + text.length() + " preview=" + truncate(text, 200));
        return text;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "null";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}