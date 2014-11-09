package com.example.musichackday;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.util.Log;

import com.google.gson.Gson;

public class NetworkParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    Document doc;

    public NetworkParser() {
    }

    public static JSONObject getJSONFromUrl(String url) {
        try {

            // Setting up a default client to get the data
            DefaultHttpClient httpClient = new DefaultHttpClient();
            // HttpPost is a request to the web server
            HttpGet httpGet = new HttpGet(url);

            // Client executes the request
            HttpResponse httpResponse = httpClient.execute(httpGet);
            // The 'response' from the server feeds back data stored in the httpEntity
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();			

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
        }

        // Assigning that string to the JSON Object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
        }

        // return JSON object which carries the JSON data
        return jObj;

    }
    
    public static TrackData getTrackDataFromUrl(String url) {
        try {

            // Setting up a default client to get the data
            DefaultHttpClient httpClient = new DefaultHttpClient();
            // HttpPost is a request to the web server
            HttpGet httpGet = new HttpGet(url);

            // Client executes the request
            HttpResponse httpResponse = httpClient.execute(httpGet);
            // The 'response' from the server feeds back data stored in the httpEntity
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();           

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.wtf("GSON",  "GSON: " + e);
        }

        Gson gson = new Gson();
        
        Log.wtf("GSON",  "GSON: " + json);
        
        return gson.fromJson(json, TrackData.class);

    }
    
    public static TrackLyrics getTrackLyricsFromUrl(String url) {
        try {

            // Setting up a default client to get the data
            DefaultHttpClient httpClient = new DefaultHttpClient();
            // HttpPost is a request to the web server
            HttpGet httpGet = new HttpGet(url);

            // Client executes the request
            HttpResponse httpResponse = httpClient.execute(httpGet);
            // The 'response' from the server feeds back data stored in the httpEntity
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.wtf("GSON",  "GSON: " + e);
        }

        Gson gson = new Gson();
        
        Log.wtf("GSON",  "GSON: " + json);
        
        return gson.fromJson(json, TrackLyrics.class);

    }
    
}
