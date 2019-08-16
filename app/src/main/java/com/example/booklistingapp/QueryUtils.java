package com.example.booklistingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class QueryUtils {
    private static final String TAG = "QueryUtils";

    static public ArrayList<Book> extractBooksFromURL(String url) {
        String jsonData = makeHTTPConnection(createURL(url));
        Log.i(TAG, "extractBooksFromURL: JSONDATA: " + jsonData);
        return parseJSONData(jsonData);
    }

    static private String makeHTTPConnection(URL url) {
        if (url == null) {
            return "";
        }

        HttpURLConnection urlConnection = null;
        String responseString = "";
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.i(TAG, "makeHTTPConnection: RESPONSE CODE = " + urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))){
                    StringBuilder input = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        input.append(line);
                        line = bufferedReader.readLine();
                    }
                    responseString = input.toString();
                } catch (IOException e) {
                    Log.e(TAG, "makeHTTPConnection: error reading the data from stream");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "makeHTTPConnection: Error");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return responseString;
    }

    static private ArrayList<Book> parseJSONData(String data) {
        try {
            ArrayList<Book> books = new ArrayList<>();
            JSONObject initialJson = new JSONObject(data);
            JSONArray bookItems = initialJson.getJSONArray("items");
            for (int i=0; i<bookItems.length(); i++) {
                JSONObject book = (JSONObject) bookItems.get(i);
                JSONObject volumeInfo = (JSONObject) book.getJSONObject("volumeInfo");

                if (!volumeInfo.has("title"))  {
                    continue;
                }

                List<String> authors = null;
                String description = "", imageURL = "";
                int pageCount = Book.INT_FIELD_NOT_PROVIDED;
                Date publishedDate = null;

                String title = volumeInfo.getString("title");
                if (volumeInfo.has("authors")) {
                    authors = jsonArrayToStringList(volumeInfo.getJSONArray("authors"));
                }
                if (volumeInfo.has("description")) {
                    description = volumeInfo.getString("description");
                }
                if (volumeInfo.has("pageCount")) {
                    pageCount = volumeInfo.getInt("pageCount");
                }
                if (volumeInfo.has("publishedDate")) {
                    publishedDate = extractDate(volumeInfo.getString("publishedDate"));
                }
                if (volumeInfo.has("imageLinks")) {
                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                     imageURL = imageLinks.getString("thumbnail");
                }
                books.add(new Book(title, authors, description, publishedDate, pageCount, imageURL));
            }
            return books;
        } catch (JSONException e) {
            Log.e(TAG, "parseJSONData: Error occured");
            e.printStackTrace();
            return null;
        }
    }

    //download bitmap image from the given URL
//    static private Bitmap downloadImage(String url) {
//        try {
//            InputStream in = new java.net.URL(url).openStream();
//            return BitmapFactory.decodeStream(in);
//        } catch (IOException e) {
//            Log.e(TAG, "downloadImage: error downloading the image from " + url);
//            return null;
//        }
//    }

    //Extract date from string (type: 2000-05-10)
    static private Date extractDate(String date) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "extractDate: Error parsing the date: " +  date);
            return null;
        }
    }

    static private List<String> jsonArrayToStringList(JSONArray jsonArray) throws JSONException {
        List<String> array = new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++) {
            array.add(jsonArray.get(i).toString());
        }
        return array;
    }

    static private URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "createURL: invalud URL");
            return null;
        }

    }
}
