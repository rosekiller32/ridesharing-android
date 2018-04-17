package org.beiwe.app.ui.handlers;

import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.beiwe.app.networking.GetRequest;
import org.beiwe.app.networking.HTTPUIAsync;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by devsen on 4/2/18.
 */

public class TransitManager {
    private static Fragment fragment;
    private static TransitManager instance;
    private static String GOOGLE_KEY = "AIzaSyDRJSnmycmwl9emHWdzBErbzUdT7rRGJa0";
    private static String GOOGLE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static String TRANSIT_KEY = "HYRtJEhgVk8YrXqRZqZMnK5a5";
    private static String TRANSIT_URL = "https://truetime.portauthority.org/bustime/api/v3/getstops?";

    public static TransitManager getInstance(Fragment f) {
        if (instance == null) {
            instance = new TransitManager();
        }
        fragment = f;
        return instance;
    }

    public void getTransitData(LatLng start, LatLng end, TransitManagerListener listener) {
        tryToGetTransitWithTheServer(GOOGLE_URL, start, end, new Date(), listener);
    }

    public void getTransitData(LatLng start, LatLng end, Date departureTime, TransitManagerListener listener) {
        tryToGetTransitWithTheServer(GOOGLE_URL, start, end, departureTime, listener);
    }

    public void getTransitStops(List<Step> transits, LatLng start, LatLng end, TransitManagerListener listener) {
        tryBusDetails(TRANSIT_URL, new ArrayList<List<Stop>>(), transits, start, end, listener);
    }

    private void parseResponse(JSONObject response, TransitManagerListener listener) throws JSONException {
        JSONArray routesList = response.getJSONArray("routes");
        List<Routes> transitRoutes = new ArrayList<Routes>();
        for (int i = 0; i < routesList.length(); i++) {
            transitRoutes.add(new Routes(routesList.getJSONObject(i)));
        }
        listener.onResponseRoutes(transitRoutes);
        //adapter.notifyDataSetChanged();
    }

    private List<Stop> parseStops(JSONObject response, Step transit, TransitManagerListener listener) {
        try {
            JSONArray stopsList = response.getJSONObject("bustime-response").getJSONArray("stops");
            List<Stop> transitStops = new ArrayList<Stop>();
            for (int i = 0; i < stopsList.length(); i++) {
                transitStops.add(new Stop(transit.getBus(), stopsList.getJSONObject(i)));
            }

            transitStops.sort(new Comparator<Stop>() {
                @Override
                public int compare(Stop o1, Stop o2) {
                    return o1.getStopId() - o2.getStopId();
                }
            });

            Stop start = transitStops.stream()
                    .filter(t -> t.getStopName().toLowerCase().contentEquals(transit.getDepartureStop().toLowerCase()))
                    .findFirst()
                    .orElse(transitStops.get(0));

            Stop stop = transitStops.stream()
                    .filter(t -> t.getStopName().toLowerCase().contentEquals(transit.getArrivalStop().toLowerCase()))
                    .findFirst()
                    .orElse(transitStops.get(transitStops.size() - 1));

            double distance = getDistance(start.getStop(), stop.getStop());

            List<Stop> stops = new ArrayList<Stop>();

            for (Stop s : transitStops) {
                if (getDistance(start.getStop(), s.getStop()) <= distance) {
                    stops.add(s);
                }
            }
            Log.d("Stops1", stops.toString());
            return stops;

        } catch (Exception e) {
            Log.e("Location", e.toString());
        }
        return new ArrayList<Stop>();
    }

    private double getDistance(LatLng start, LatLng end) {
        float[] distance = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, distance);
        return distance[0];
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryToGetTransitWithTheServer(final String url, LatLng start, LatLng end, Date departureTime, TransitManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                parameters= GetRequest.makeParameter("key", GOOGLE_KEY) +
                        GetRequest.makeParameter("origin", start.latitude + "," + start.longitude) +
                        GetRequest.makeParameter("destination", end.latitude + "," + end.longitude) +
                        GetRequest.makeParameter("departure_time", departureTime.getTime() / 1000l + "") +
                        GetRequest.makeParameter("mode", "transit");
                response = GetRequest.httpGetResponse(parameters, url, new ArrayList<String>());
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                //Toast.makeText(getApplicationContext(), responseCode + "", Toast.LENGTH_SHORT).show();
                try {
                    parseResponse(response, listener);
                } catch (Exception e) {
                    Log.e("Location", e.toString());
                }
            }
        };
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryBusDetails(final String url, final List<List<Stop>> stopsList, final List<Step> transits, LatLng start, LatLng end, TransitManagerListener listener) {
        Step transit = transits.get(0);

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                parameters= GetRequest.makeParameter("key", TRANSIT_KEY) +
                        GetRequest.makeParameter("format", "json") +
                        GetRequest.makeParameter("rt", transit.getBus()) +
                        GetRequest.makeParameter("rtpidatafeed", "Port%20Authority%20Bus") +
                        GetRequest.makeParameter("dir", transit.getBusType());
                response = GetRequest.httpGetResponse(parameters, url, new ArrayList<String>());
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                //Toast.makeText(getApplicationContext(), responseCode + "", Toast.LENGTH_SHORT).show();
                transits.remove(0);
                stopsList.add(parseStops(response, transit, listener));
                if (transits.isEmpty()) {
                    List<Stop> stops = new ArrayList<Stop>();
                    for (List<Stop> s : stopsList) {
                        Iterator<Stop> iterator = s.iterator();
                        int count = s.size();
                        int pos = count / 4;
                        int i = 0;
                        while (iterator.hasNext()) {
                            Stop stop = iterator.next();
                            double startDistance = getDistance(start, stop.getStop());
                            double endDistance = getDistance(end, stop.getStop());
                            if (startDistance < 1000 || endDistance < 1000) {
                                iterator.remove();
                            }
                        }
                        stops.addAll(s);
                        Log.d("Stops", stops.toString());
                    }
                    listener.onResponseStops(stops);
                }
                else {
                    tryBusDetails(url, stopsList, transits, start, end, listener);
                }
            }
        };
    }

    public interface TransitManagerListener {
        // TODO: Update argument type and name
        void onResponseRoutes(List<Routes> routes);
        void onResponseStops(List<Stop> stops);
    }
}
