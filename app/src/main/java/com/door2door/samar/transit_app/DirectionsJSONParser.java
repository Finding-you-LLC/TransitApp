package com.door2door.samar.transit_app;

/**
 * Created by samar on 10/9/2017.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class DirectionsJSONParser {
    public List<List<HashMap<String, String>>> parse(JSONObject jObject,String Travelmode,String Destination) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jseg = null;
        JSONArray jStops = null;
        String TravelMode="";
        String polyline = "";
        List path = new ArrayList<HashMap<String, String>>();
        try {

            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jseg = ((JSONObject) jRoutes.get(i)).getJSONArray("segments");

                /** Traversing all segments */
                for (int j = 0; j < jseg.length(); j++) {
                    jStops= ((JSONObject) jseg.get(j)).getJSONArray("stops");
                    polyline = (String) ( ((JSONObject) jseg
                            .get(j)).get("polyline"));
                    String travel_mode= polyline = (String) ( ((JSONObject) jseg
                            .get(j)).get("travel_mode"));
                    if(travel_mode.equals(Travelmode)) {
                        List<LatLng> list = decodePoly(polyline);
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat",
                                    Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng",
                                    Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                        /** Traversing all Stops */
                        for (int k = 0; k < jStops.length(); k++) {
                            //here we can get all data related to each stop station to display it on the map if needed according to the application requirements
                            /** Traversing all points */
                        }
                        routes.add(path);
                    }
                    else{
                        Log.e("Error","Couldn't find Destination ");
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return routes;
    }
     // Method to decode polyline points
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
