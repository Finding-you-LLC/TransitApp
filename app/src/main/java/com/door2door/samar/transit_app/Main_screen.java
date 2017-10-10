package com.door2door.samar.transit_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Main_screen extends FragmentActivity implements View.OnClickListener
        {
            private GoogleMap map;
            private ArrayList<LatLng> markerPoints;
            private GPSTracker gps; // this is current location
            private double latitude, longitude;
            private double longitude2, latitude2;
            String url = "https://github.com/door2door-io/transit-app-task/blob/master/data.json";
            private Button btnShow;
            private EditText Destination1;
            String tag_json_arry = "json_array_req";
            private String Data="";
            String travelmode="";
            String Destination="";
            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        gps = new GPSTracker(Main_screen.this);
                Spinner dropdown = (Spinner)findViewById(R.id.spinner1);
                btnShow = (Button) findViewById(R.id.button1);
                Destination1 = (EditText) findViewById(R.id.Destination);
        if (gps.canGetLocation()) {
            latitude = gps.getLocation().getLatitude(); // hear i got lat lng from last activity.
            longitude = gps.getLocation().getLongitude();
            Log.e("View Map Direction", "get the Lat and Lng :" + latitude + " :: " + longitude);
        }else{
            gps.showSettingsAlert();
        }
                String[] items = new String[]{"walking", "subway", "bus","change","setup","driving","cycling"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
                dropdown.setAdapter(adapter);
                travelmode = dropdown.getSelectedItem().toString();
                Destination=Destination1.getText().toString();
                SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // Getting Map for the SupportMapFragment
        map = fm.getMap();
        btnShow.setOnClickListener(this);
    }





            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button1:
                        finish();
                        break;
                    default:
                        break;
                }
            }

            private String downloadUrl(String strUrl) throws IOException {
                final ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage("Loading...");
                pDialog.show();

                JsonArrayRequest req = new JsonArrayRequest(url,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("samartask", response.toString());
                                Data=response.toString();
                                pDialog.hide();
                                JSONArray ja_data = response;
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("samartask", "Error: " + error.getMessage());
                        pDialog.hide();
                    }
                });
// Adding request to request queue
                AppController.getInstance().addToRequestQueue(req, tag_json_arry);
                return Data;
            }
            @Override
            protected void onResume() {

                super.onResume();
                markerPoints = new ArrayList<LatLng>();
                // Getting reference to SupportMapFragment of the
                // activity_main
                if (map != null) {
                                      // Enable MyLocation Button in the Map
                    map.setMyLocationEnabled(true);
                    LatLng CardPoint = new LatLng(latitude, longitude);

                    Log.e(" in View Map ", " in On Card Point........." + CardPoint.latitude);

                    MarkerOptions Destpoint = new MarkerOptions();
                    Destpoint.position(CardPoint);
                    map.addMarker(Destpoint);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(CardPoint, 10));

                    LatLng UserPoint = new LatLng(latitude2, longitude2);
                    MarkerOptions Sourcepoint = new MarkerOptions();
                    Sourcepoint.position(UserPoint);
                    map.addMarker(Sourcepoint);
                    String data = "";
                    try {
                        // Fetching the data from web service
                        data = downloadUrl(url);
                    } catch (Exception e) {
                        Log.e("Background Task", e.toString());
                    }
                    ParserTask parserTask = new ParserTask();
                    Log.e(" in View Map ", " in on Post  .........");
                    // Invokes the thread for parsing the JSON data
                    parserTask.execute(data);
                }
            }
            /** A class to parse the Google Places in JSON format */
            private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

                // Parsing the data in non-ui thread
                @Override
                protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                    JSONObject jObject;
                    List<List<HashMap<String, String>>> routes = null;
                    Log.e(" in View Map ", " in parser Task Calling  .........");
                    try {
                        jObject = new JSONObject(jsonData[0]);
                        DirectionsJSONParser parser = new DirectionsJSONParser();
                        // Starts parsing data
                        routes = parser.parse(jObject,travelmode,Destination);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return routes;
                }

                // Executes in UI thread, after the parsing process
                @Override
                protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                    ArrayList<LatLng> points = null;
                    PolylineOptions lineOptions = null;
                    MarkerOptions markerOptions = new MarkerOptions();
                    Log.e(" in View Map ", " in parser post ........." + result.size());
                    // Traversing through all the routes
                    for (int i = 0; i < result.size(); i++) {
                        Log.e(" in View Map ", " in Result For .........");
                        points = new ArrayList<LatLng>();
                        lineOptions = new PolylineOptions();

                        // Fetching i-th route
                        List<HashMap<String, String>> path = result.get(i);

                        Log.e(" in View Map ", " in path  ........." + path.size());
                        // Fetching all the points in i-th route
                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            Log.e("in View Direction ", " lag ,,,,, " + lat + " lng ... " + lng);
                            points.add(position);
                        }

                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(4);
                        lineOptions.color(Color.BLUE);
                    }

                    // Drawing polyline in the Google Map for the i-th route
                    if (map != null) {
                        if (gps.canGetLocation()) {
                            latitude2 = gps.getLatitude();
                            longitude2 = gps.getLongitude();

                            Log.e("View Map Direction ", " get Current Lat lng " + latitude2 + " lng " + longitude2);
                            if ((latitude != 0) && (longitude != 0)) {
                                map.addPolyline(lineOptions);
                            }
                        } else {
                            gps.showSettingsAlert();
                        }
                    }

                }
            }

        }
