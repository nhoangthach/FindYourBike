package com.android.app.fybike.map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleAdapter;

import com.android.app.fybike.Controller.ShopController;
import com.android.app.fybike.model.ShopModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Thach Nguyen on 11/14/2016.
 */

public class MapHandler {

    private GoogleMap m_GoogleMap;
    private double m_Latitude;
    private double m_Longitude;
    private String m_type;
    private Context m_context;
    private AutoCompleteTextView m_atvPlaces;
    private CharSequence m_s;

    public MapHandler(double mLatitude, double mLongitude, String type, GoogleMap map){
        m_GoogleMap = map;
        m_Latitude = mLatitude;
        m_Longitude = mLongitude;
        m_type = type;
    }

    public MapHandler(Context context, AutoCompleteTextView atvPlaces,CharSequence s){
        m_context = context;
        m_atvPlaces = atvPlaces;
        m_s = s;
    }

    public MapHandler(double mLatitude, double mLongitude,GoogleMap mGoogleMap, Context context) {
        m_GoogleMap = mGoogleMap;
        m_context = context;
        m_Latitude = mLatitude;
        m_Longitude = mLongitude;
    }

    public void showNearPlaces(){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location="+m_Latitude+","+m_Longitude);
        sb.append("&radius=5000");
        sb.append("&types="+m_type);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyBeTHQcybG65xNKJhAeyR0ImcdZdCUq6vA");


        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
    }

    public void showAutoCompletePlaces(){
        AutoCompletePlacesTask placesTask = new AutoCompletePlacesTask();
        placesTask.execute(m_s.toString());
    }

    public void showQuerryPlaces(){
        QueryMap queryMap = new QueryMap();
        queryMap.execute(m_Longitude, m_Latitude);
    }

    public void getLocalLocation(){
        StringBuilder sb = new StringBuilder("https://for-your-bike-map.herokuapp.com/api/locations");

        LocalLocation localLocation = new LocalLocation();

        // Invokes the "doInBackground()" method of the class PlaceTask
        localLocation.execute(sb.toString());
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);


            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception when down url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }


    /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            // Clears all the existing markers
//            m_GoogleMap.clear();

            for(int i=0;i<list.size();i++){

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);

                // Placing a marker on the touched position
                m_GoogleMap.addMarker(markerOptions);

            }

        }

    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class AutoCompletePlacesTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyBeTHQcybG65xNKJhAeyR0ImcdZdCUq6vA";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            String language ="language=vi";


            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+language+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
            try{
                // Fetching the data from web service in background
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            AutoCompleteParserTask parserTask = new AutoCompleteParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }


    /** A class to parse the Google Places in JSON format */
    private class AutoCompleteParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parseAutoComplete(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(m_context, result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            m_atvPlaces.setAdapter(adapter);
        }
    }

    /** A class, to download Google Places */
    private class LocalLocation extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            LocalLocationParserTask parserTask = new LocalLocationParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /** A class to parse the Google Places in JSON format */
    private class LocalLocationParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;
        JSONArray jsonArray;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
//                jObject = new JSONObject(jsonData[0]);
                jsonArray = new JSONArray(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parseLocalLocation(jsonArray);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            // Clears all the existing markers
//            m_GoogleMap.clear();

            for(int i=0;i<list.size();i++){

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
//                String name = hmPlace.get("place_name");

                // Getting vicinity
//                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
//                markerOptions.title(name + " : " + vicinity);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                // Placing a marker on the touched position
                m_GoogleMap.addMarker(markerOptions);

            }

        }

    }

    private class QueryMap extends AsyncTask<Double, Void ,String> {
        final String API_URL = "https://for-your-bike-map.herokuapp.com/api/query";
        String server_response;
        ArrayList<ShopModel> listShop;

        @Override
        protected String doInBackground(Double... arg) {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                JSONArray types = new JSONArray();
                types.put(0, "Motobike");
                url = new URL(API_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoInput (true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();
                JSONObject jsonparam = new JSONObject();
                jsonparam.put("longitude", arg[0]);
                jsonparam.put("latitude", arg[1]);
                jsonparam.put("distance", 1000);
                jsonparam.put("types", types);
                jsonparam.put("minRating", 1);
                jsonparam.put("maxRating", 5);
                Log.e("Post", "" + jsonparam.toString());
                BufferedWriter out =
                        new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                out.write(jsonparam.toString());
                out.close();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                server_response = readStream(in);
                Log.e("Respone", "" + server_response);
                ShopController shopController = ShopController.Instance();
                listShop = shopController.ParseObject(server_response);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return server_response;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Response", "" + server_response);
            for (ShopModel shop : listShop)
            {
                LatLng latLng = new LatLng(shop.getLatitue(), shop.getLongitue());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(shop.getNameShop());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                m_GoogleMap.addMarker(markerOptions);
            }
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }

    }

}
