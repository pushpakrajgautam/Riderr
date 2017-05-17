package io.github.maniknarang.riderr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.stripe.android.model.Token;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class MapFragment extends SupportMapFragment implements  GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback
{
    public GoogleApiClient mGoogleApiClient;
    public Location mCurrentLocation;
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private int curMapTypeIndex = 1;
    public LocationRequest mLocationRequest;
    private boolean mapped = false;
    private String url;
    private Marker marker;
    private GoogleMap googleMap;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private LocationManager locationManager;
    private MapActivity mapActivity;
    public String urlMetro;

    public void alertLocRequest()
    {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings
                (mGoogleApiClient, builder.build());

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mapActivity = (MapActivity) getActivity();
        setHasOptionsMenu(true);
        getMapAsync(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        //markerPoints.clear();
        //getMap().clear();
        //LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        //markerPoints.add(origin);
        //markerPoints.add(latLng);
        initMarker(latLng);
        /*if (markerPoints.size() >= 2) {
            LatLng dest = markerPoints.get(1);
            url = getDirectionsUrl(origin, dest);
            mapped=true;
            Toast.makeText(getContext(),"Long tap to view results",Toast.LENGTH_LONG).show();
         }*/
        // sequence - results(array) - geometry - location - lat lng
        // dir url = https://maps.googleapis.com/maps/api/directions/json?origin=Magadi+Main+Rd,Bengaluru,Karnataka,India&destination=Majestic,Bengaluru,Karnataka,India&mode=transit&transit_mode=subway&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.googleMap=googleMap;
        //googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.maps));
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        /*if(mapped)
        {
            mapped=false;
            Intent intent = new Intent(getActivity(),ResultActivity.class);
            intent.putExtra("JsonUrl",url);
            startActivity(intent);
        }*/
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        /*markerPoints.clear();
        getMap().clear();
            LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            markerPoints.add(origin);
            LatLng latLng = marker.getPosition();
            markerPoints.add(latLng);
            MarkerOptions options = new MarkerOptions().position(latLng);
            options.title(getAddressFromLatLng(latLng));
            options.icon(BitmapDescriptorFactory.fromBitmap(generateBitmapFromDrawable(R.drawable.ic_house)));
            getMap().addMarker(options).setDraggable(true);
        if(markerPoints.size() >= 2)
        {
            LatLng dest = markerPoints.get(1);
            String url = getDirectionsUrl(origin, dest);
            mapped=true;
            Toast.makeText(getContext(),"Long tap to view results",Toast.LENGTH_LONG).show();
        }*/

        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;
        urlMetro = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng +
                "&radius=50000&type=subway_station&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
        String urlBus = "http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + lat +"/lon/" + lng + "/rad/1";
        new NearbyPlacesTask().execute(urlMetro,urlBus);
    }

    public void initCamera(Location mCurrentLocation)
    {
        try
        {
            googleMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e) {}
        CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude())).zoom(16f).bearing(0.0f).tilt(0.0f).build();
        googleMap.setPadding(0,0,0,0);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), null);
        googleMap.setMapType( MAP_TYPES[curMapTypeIndex] );
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnInfoWindowClickListener( this );
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        initMarker(latLng);
    }

    private String getAddressFromLatLng( LatLng latLng )
    {
        Geocoder geocoder = new Geocoder( getActivity() );

        String address = "";
        try
        {
            if(!geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).isEmpty())
            address = geocoder
                    .getFromLocation( latLng.latitude, latLng.longitude, 1 )
                    .get( 0 ).getAddressLine( 0 );
        }
        catch (IOException e )
        {
        }

        return address;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest)
    {
        // Origin of route
        String str_origin = "&origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+
                "&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";

        return url;
    }

    private Bitmap generateBitmapFromDrawable(int drawablesRes)
    {
        Bitmap bitmap;
        Drawable drawable = getResources().getDrawable(drawablesRes);
        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        bitmap = Bitmap.createScaledBitmap(bitmap,2*bitmap.getWidth(),2*bitmap.getHeight(),true);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void initMarker(LatLng latLng)
    {
        if(marker != null)
            marker.remove();
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));
        options.icon(BitmapDescriptorFactory.fromBitmap(generateBitmapFromDrawable(R.drawable.ic_house)));
        marker = googleMap.addMarker(options);
        marker.setDraggable(true);

        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;
        urlMetro = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng +
                "&radius=50000&type=subway_station&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
        //mapActivity.stops.add(new Stop("Mantri","Naga-Mantr","01:20 am", "5", "PURPLE"));
        //mapActivity.stopAdapter.notifyDataSetChanged();
        new NearbyPlacesTask().execute(urlMetro);
    }

    public void createTask(String url)
    {
        new NearbyPlacesTask().execute(url);
    }
    public class NearbyPlacesTask extends AsyncTask<String,Void,ArrayList<Stop>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mapActivity.stops.clear();
            mapActivity.stopAdapter.notifyDataSetChanged();
        }

        @Override
        protected ArrayList<Stop> doInBackground(final String... urls)
        {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10,TimeUnit.SECONDS).writeTimeout(10,TimeUnit.SECONDS).build();
            Request request = new Request.Builder()
                    .url(urls[0])
                    .build();
            try
            {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for(int i=0; i<jsonArray.length();i++)
                {
                    //mapActivity.stops.add(new Stop("Mantri","Naga-Mantr","01:20 am", "5", "PURPLE"));
                    String name = jsonArray.getJSONObject(i).getString("name");
                    String subwayUrl = "http://prgzz.eastus.cloudapp.azure.com/api-transport/api/metro/" + name;
                    Request request1 = new Request.Builder()
                            .url(subwayUrl)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String responseData1 = response1.body().string();
                    JSONArray jsonArray1 = new JSONArray(responseData1);
                        if(jsonArray1.getJSONObject(0) != null)
                        {
                            ArrayList<String> times = new ArrayList<>();
                            String route = jsonArray1.getJSONObject(0).getString("UP");
                            String line = jsonArray1.getJSONObject(0).getString("line");
                            String orderNo = jsonArray1.getJSONObject(0).getString("order_no");
                            String polyline = jsonArray1.getJSONObject(0).getString("polyline");
                            JSONArray jArray = jsonArray1.getJSONObject(0).getJSONArray("time");
                            for (int k = 0; k < jArray.length(); k++)
                                times.add(jArray.getString(k));
                            String time = jArray.getString(0);
                            double lat = jsonArray1.getJSONObject(0).getDouble("lat");
                            double lng = jsonArray1.getJSONObject(0).getDouble("lng");
                            if(jsonArray1.getJSONObject(1) != null)
                            {
                                ArrayList<String> opptimes = new ArrayList<>();
                                JSONArray jArray5 = jsonArray1.getJSONObject(1).getJSONArray("time");
                                String oppOrder = jsonArray1.getJSONObject(1).getString("order_no");
                                for (int k = 0; k < jArray5.length(); k++)
                                    opptimes.add(jArray5.getString(k));
                                String otherTime = jArray5.getString(0);
                                Stop stop = new Stop(name,route,time,otherTime,orderNo,oppOrder,line,times,opptimes,
                                        polyline,lat,lng);
                                mapActivity.stops.add(stop);
                            }
                        }
                }
            }
            catch (IOException e){}
            catch (JSONException e){}

            return mapActivity.stops;
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops)
        {
            mapActivity.stopAdapter.notifyDataSetChanged();
            mapActivity.swipeRefreshLayout.setRefreshing(false);
        }

    }

    public void makeRoute(ArrayList<LatLng> pointList,double lat, double lng)
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(pointList).width(20).color(getResources().getColor(R.color.colorPrimary));
        googleMap.addPolyline(polylineOptions);
        for(int i=0; i<pointList.size();i+=100)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pointList.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            googleMap.addMarker(markerOptions);
        }
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(lat,lng))
                .zoom(12.5f).bearing(0.0f).tilt(0.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
    }
}