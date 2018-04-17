package org.beiwe.app.ui.handlers;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.beiwe.app.networking.GetRequest;
import org.beiwe.app.networking.HTTPUIAsync;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by devsen on 4/2/18.
 */

public class UberManager {
    private static UberManager instance;
    private static Fragment fragment;
    private static final String UBER_PRODUCT_URL = "https://api.uber.com/v1.2/products?";
    private static final String UBER_URL_ETA = "https://api.uber.com/v1.2/estimates/time?";
    private static final String UBER_URL_PRICE = "https://api.uber.com/v1.2/estimates/price?";
    private static final String UBER_KEY = "Bearer KA.eyJ2ZXJzaW9uIjoyLCJpZCI6InY3Y3hEU1FQUlUrOWR1WXRoMUtyQmc9PSIsImV4cGlyZXNfYXQiOjE1MjU3NTA3ODAsInBpcGVsaW5lX2tleV9pZCI6Ik1RPT0iLCJwaXBlbGluZV9pZCI6MX0.ARDssICrIWPCrll3G3HGCqnFA5t_zGwRZmbfWDW8A9E";

    public static UberManager getInstance(Fragment f) {
        if (instance == null) {
            instance = new UberManager();
            fragment = f;
        }
        return instance;
    }

    public void getUberRoutes(LatLng start, LatLng end, UberManagerListener listener) {
        tryUberProductDetails(UBER_PRODUCT_URL, start, end, listener);
    }

    public interface UberManagerListener {
        // TODO: Update argument type and name
        void onResponse(Step step);
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryUberProductDetails(final String url, LatLng start, LatLng end, UberManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                parameters= GetRequest.makeParameter("latitude", start.latitude + "") +
                            GetRequest.makeParameter("longitude", start.longitude + "");

                List<String> headers = new ArrayList<String>();
                headers.add("Authorization," + UBER_KEY);
                response = GetRequest.httpGetResponse(parameters, url, headers);
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                //Toast.makeText(getApplicationContext(), responseCode + "", Toast.LENGTH_SHORT).show();
                //parseStops(response, transit, listener);
                parseProducts(response, start, end, listener);
            }
        };
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryUberEtaDetails(final String url, String productId, LatLng start, LatLng end, UberManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    parameters= GetRequest.makeParameter("start_latitude", start.latitude + "") +
                            GetRequest.makeParameter("start_longitude", start.longitude + "");

                    List<String> headers = new ArrayList<String>();
                    headers.add("Authorization," + UBER_KEY);
                    response = GetRequest.httpGetResponse(parameters, url, headers);
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                parseEtaRoutes(response, productId, start, end, listener);
            }
        };
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryUberDetails(final String url, String productId, double estimate, LatLng start, LatLng end, UberManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    parameters= GetRequest.makeParameter("start_latitude", start.latitude + "") +
                            GetRequest.makeParameter("start_longitude", start.longitude + "") +
                            GetRequest.makeParameter("end_latitude", end.latitude + "") +
                            GetRequest.makeParameter("end_longitude", end.longitude + "");

                    List<String> headers = new ArrayList<String>();
                    headers.add("Authorization," + UBER_KEY);
                    response = GetRequest.httpGetResponse(parameters, url, headers);
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                //Toast.makeText(getApplicationContext(), responseCode + "", Toast.LENGTH_SHORT).show();
                //parseStops(response, transit, listener);
                parseUberRoutes(response, productId, estimate, listener);
            }
        };
    }

    private void parseProducts(JSONObject response, LatLng start, LatLng end, UberManagerListener listener) {
        try {
            JSONArray products = response.getJSONArray("products");
            JSONObject product = products.getJSONObject(0);
            String productId = product.getString("product_id");

            tryUberEtaDetails(UBER_URL_ETA, productId, start, end, listener);
        } catch (Exception e) {

        }
    }

    private void parseEtaRoutes(JSONObject response, String productId, LatLng start, LatLng end, UberManagerListener listener) {
        try {
            double estimate = 0;
            JSONArray estimates = response.getJSONArray("times");
            for (int i = 0; i < estimates.length(); i++) {
                JSONObject time = estimates.getJSONObject(i);
                if (time.getString("product_id").contentEquals(productId)) {
                    estimate = time.getDouble("estimate");
                    break;
                }
            }
            tryUberDetails(UBER_URL_PRICE, productId, estimate, start, end, listener);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    private void parseUberRoutes(JSONObject response, String productId, double estimate, UberManagerListener listener) {
        try {
            Double fareValue = 0d;
            Double duration = 0d;
            Double distance = 0d;
            JSONArray prices = response.getJSONArray("prices");
            for (int i = 0; i < prices.length(); i++) {
                JSONObject time = prices.getJSONObject(i);
                if (time.getString("product_id").contentEquals(productId)) {
                    fareValue = time.getDouble("high_estimate");
                    duration = time.getDouble("duration");
                    distance = time.getDouble("distance");
                    break;
                }
            }
            createStep(fareValue, estimate, duration, distance, listener);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    private void createStep(Double fare, Double estimate, Double duration, Double distance, UberManagerListener listener) {
        Step s = new Step(estimate, duration);
        s.setType("UBER");
        s.setDuration(duration);
        s.setDistance(distance * 1609);
        s.setFare(fare);
        listener.onResponse(s);
    }
}
